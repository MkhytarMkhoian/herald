package io.github.mkhytarmkhoian.herald

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * The fan-out. Holds every registered vendor and forwards each call to all of them.
 *
 * It implements all five capabilities, so one instance can be bound to whichever of them your
 * classes depend on. Bind it to those interfaces and depend on the interfaces: a ViewModel takes
 * an [EventTrackerService], not a `Herald`.
 *
 * Build one with the [Herald] function.
 */
public class Herald internal constructor(
    private val providers: List<Provider>,
    private val dispatcher: CoroutineDispatcher,
    private val errorReporter: AnalyticsErrorReporter,
) : EventTrackerService,
    PropertyTrackerService,
    IdentifiableUserService,
    AnalyticsLifecycleService,
    ConsentService {

    /**
     * One registered vendor: a name and whichever of the five capabilities it has.
     *
     * Built for you by [HeraldBuilder.provider]. Construct it directly when providers are
     * collected elsewhere — a DI multibinding, say — and handed over as a batch with
     * [HeraldBuilder.providers].
     */
    public class Provider(
        public val name: String,
        public val events: EventTrackerService? = null,
        public val properties: PropertyTrackerService? = null,
        public val identity: IdentifiableUserService? = null,
        public val lifecycle: AnalyticsLifecycleService? = null,
        public val consent: ConsentService? = null,
    ) {
        init {
            require(name.isNotBlank()) { "A provider needs a name; it is what identifies it in a failure report." }
            require(
                events != null || properties != null || identity != null ||
                    lifecycle != null || consent != null
            ) {
                "Provider '$name' was registered with no capabilities, so it can never be called."
            }
        }
    }

    override suspend fun track(event: Event): Unit =
        onEachProvider(AnalyticsOperation.Track(event.name)) { events?.track(event) }

    override suspend fun set(property: Property): Unit =
        onEachProvider(AnalyticsOperation.SetProperty(property.name)) { properties?.set(property) }

    override suspend fun identify(identity: Identity): Unit =
        onEachProvider(AnalyticsOperation.Identify) { this.identity?.identify(identity) }

    override suspend fun reset(): Unit =
        onEachProvider(AnalyticsOperation.Reset) { identity?.reset() }

    override suspend fun start(): Unit =
        onEachProvider(AnalyticsOperation.Start) { lifecycle?.start() }

    override suspend fun setEnabled(enabled: Boolean): Unit =
        onEachProvider(AnalyticsOperation.SetEnabled(enabled)) { consent?.setEnabled(enabled) }

    override suspend fun flush(): Unit =
        onEachProvider(AnalyticsOperation.Flush) { lifecycle?.flush() }

    /**
     * Runs [block] against every provider on [dispatcher], catching what any one of them throws so
     * the rest still run.
     *
     * Providers run concurrently, but the call suspends until all of them finish, so a second
     * [track] cannot start before the first has reached every vendor.
     *
     * Failures go to [errorReporter] afterwards, one at a time, in registration order. Cancelling
     * the caller's coroutine propagates rather than being reported as a vendor failure.
     */
    private suspend fun onEachProvider(
        operation: AnalyticsOperation,
        block: suspend Provider.() -> Unit,
    ) = withContext(dispatcher) {
        val outcomes = providers.map { provider ->
            async {
                try {
                    provider.block()
                    null
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (failure: Exception) {
                    provider.name to failure
                }
            }
        }.awaitAll()

        // Not covered by the `try` above: that one is inside `async` and wraps the vendor call,
        // while reporting runs after `awaitAll` so the reporter is never called from two threads at
        // once. No cancellation branch: nothing here suspends, so the only CancellationException
        // possible is one a reporter throws by hand, which is a reporter failure like any other.
        outcomes.filterNotNull().forEach { (name, failure) ->
            try {
                errorReporter.onFailure(name, operation, failure)
            } catch (_: Exception) {
                // The reporter is the reporting channel, so a failure here has nowhere to go.
            }
        }
    }
}

/**
 * Registers the vendors Herald should fan out to.
 *
 * ```kotlin
 * val herald = Herald {
 *     provider(
 *         name = "firebase",
 *         events = firebaseTracker,
 *         properties = firebaseTracker,
 *         identity = firebaseService,
 *         lifecycle = firebaseService,
 *     )
 *     provider(name = "adjust", events = adjustTracker)
 *     dispatcher(Dispatchers.IO)
 *     errorReporter { provider, operation, failure -> crashlytics.recordException(failure) }
 * }
 * ```
 */
public fun Herald(block: HeraldBuilder.() -> Unit): Herald = HeraldBuilder().apply(block).build()

/** Receiver of the [Herald] block. */
public class HeraldBuilder internal constructor() {

    private val providers = mutableListOf<Herald.Provider>()
    private var dispatcher: CoroutineDispatcher = Dispatchers.Default
    private var errorReporter: AnalyticsErrorReporter = AnalyticsErrorReporter.NONE

    /**
     * Adds one vendor. Pass only the capabilities it has — an adapter that implements several of
     * them is the same object passed under each.
     *
     * [name] appears in every failure handed to the error reporter, so use something you would
     * recognise in a crash report.
     *
     * Which events reach a vendor is decided by that adapter's factory chain. To gate, sample or
     * debounce, wrap the capability — [EventTrackerService] is a `fun interface`:
     *
     * ```kotlin
     * provider(name = "adjust", events = EventTrackerService { if (it !is PiiEvent) adjust.track(it) })
     * ```
     */
    public fun provider(
        name: String,
        events: EventTrackerService? = null,
        properties: PropertyTrackerService? = null,
        identity: IdentifiableUserService? = null,
        lifecycle: AnalyticsLifecycleService? = null,
        consent: ConsentService? = null,
    ) {
        providers += Herald.Provider(
            name = name,
            events = events,
            properties = properties,
            identity = identity,
            lifecycle = lifecycle,
            consent = consent,
        )
    }

    /**
     * Adds vendors already built as [Herald.Provider]s, in iteration order.
     *
     * For a composition root that collects them rather than lists them — each feature or vendor
     * module contributes one to a DI multibinding, and the root passes the set through:
     *
     * ```kotlin
     * val herald = Herald { providers(injectedProviders) }
     * ```
     *
     * Mixes freely with [provider]; registration order is call order.
     */
    public fun providers(providers: Iterable<Herald.Provider>) {
        this.providers += providers
    }

    /**
     * Where the fan-out runs. Defaults to [Dispatchers.Default], so a call from a ViewModel does
     * not put vendor SDK work on the main thread.
     *
     * Override it to use a dedicated thread, or a `TestDispatcher` to make tests deterministic.
     */
    public fun dispatcher(dispatcher: CoroutineDispatcher) {
        this.dispatcher = dispatcher
    }

    /** Where contained vendor failures go. Defaults to discarding them. */
    public fun errorReporter(errorReporter: AnalyticsErrorReporter) {
        this.errorReporter = errorReporter
    }

    internal fun build(): Herald = Herald(providers.toList(), dispatcher, errorReporter)
}
