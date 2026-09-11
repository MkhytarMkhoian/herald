package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.PropertyTrackerService
import io.github.mkhytarmkhoian.herald.asString

/**
 * A vendor that records instead of sending, for tests in apps that use Herald.
 *
 * It implements all five capabilities, so you can register it as a real provider and the test then
 * runs through the composition root you actually ship — factory chain, dispatcher and all:
 *
 * ```kotlin
 * val analytics = FakeAnalyticsProvider()
 * val herald = Herald {
 *     provider(name = "test", events = analytics, properties = analytics)
 * }
 *
 * viewModel.onPayTapped()
 *
 * analytics.assertTracked("checkout_pay_tapped") { param("plan", "pro") }
 * analytics.assertNothingElseTracked()
 * ```
 *
 * For a class that only tracks events a lambda is smaller — `EventTrackerService { recorded += it }`
 * needs no dependency. Reach for this when the test spans more than one capability, cares about
 * ordering between them, or runs through a real `Herald`.
 *
 * Safe to call from several coroutines at once.
 */
public class FakeAnalyticsProvider :
    EventTrackerService,
    PropertyTrackerService,
    IdentifiableUserService,
    AnalyticsLifecycleService,
    ConsentService {

    private val lock = Any()
    private val recorded = mutableListOf<AnalyticsRecord>()
    private val accountedFor = mutableSetOf<Int>()

    /** Everything received, oldest first. */
    public val records: List<AnalyticsRecord> get() = synchronized(lock) { recorded.toList() }

    /** Just the events, in order. */
    public val events: List<Event>
        get() = records.filterIsInstance<AnalyticsRecord.Tracked>().map { it.event }

    /** Just the properties, in order. */
    public val properties: List<Property>
        get() = records.filterIsInstance<AnalyticsRecord.PropertySet>().map { it.property }

    override suspend fun track(event: Event): Unit = record(AnalyticsRecord.Tracked(event))

    override suspend fun set(property: Property): Unit = record(AnalyticsRecord.PropertySet(property))

    override suspend fun identify(identity: Identity): Unit = record(AnalyticsRecord.Identified(identity))

    override suspend fun reset(): Unit = record(AnalyticsRecord.Reset)

    override suspend fun start(): Unit = record(AnalyticsRecord.Started)

    override suspend fun setEnabled(enabled: Boolean): Unit = record(AnalyticsRecord.EnabledSet(enabled))

    override suspend fun flush(): Unit = record(AnalyticsRecord.Flushed)

    /** Forgets everything, including which records have already been asserted on. */
    public fun clear(): Unit = synchronized(lock) {
        recorded.clear()
        accountedFor.clear()
    }

    /**
     * Asserts that exactly one event with [name] was tracked, and applies [assertions] to it.
     *
     * Exactly one, not at least one, so a duplicated event fails rather than passing quietly. Use
     * [assertTrackedTimes] when a repeat is what you meant.
     */
    public fun assertTracked(name: String, assertions: TrackedEventAssert.() -> Unit = {}) {
        val matches = trackedIndicesOf(name)
        when (matches.size) {
            0 -> fail("Expected an event named '$name', but it was never tracked.")
            1 -> {
                synchronized(lock) { accountedFor += matches.single() }
                TrackedEventAssert(name, eventAt(matches.single()), ::fail).assertions()
            }
            else -> fail(
                "Expected one event named '$name', but ${matches.size} were tracked. " +
                    "Use assertTrackedTimes(\"$name\", ${matches.size}) if that is intended."
            )
        }
    }

    /** Asserts that [name] was tracked exactly [times] times. */
    public fun assertTrackedTimes(name: String, times: Int) {
        val matches = trackedIndicesOf(name)
        if (matches.size != times) {
            fail("Expected '$name' to be tracked $times times, but it was tracked ${matches.size} times.")
        }
        synchronized(lock) { accountedFor += matches }
    }

    /** Asserts that no event with [name] was tracked. */
    public fun assertNotTracked(name: String) {
        if (trackedIndicesOf(name).isNotEmpty()) fail("Expected '$name' never to be tracked.")
    }

    /** Asserts that no event was tracked at all. */
    public fun assertNothingTracked() {
        val tracked = events
        if (tracked.isNotEmpty()) fail("Expected no events, but ${tracked.size} were tracked.")
    }

    /**
     * Asserts that every event tracked has already been named by an earlier assertion.
     *
     * Without it, an unexpected event — a duplicate, or one leaking from another feature — goes
     * unnoticed.
     */
    public fun assertNothingElseTracked() {
        val unexpected = synchronized(lock) {
            recorded.withIndex()
                .filter { (index, record) -> record is AnalyticsRecord.Tracked && index !in accountedFor }
                .map { (_, record) -> (record as AnalyticsRecord.Tracked).event.name }
        }
        if (unexpected.isNotEmpty()) {
            fail("Unexpected events tracked: ${unexpected.joinToString()}.")
        }
    }

    /**
     * Asserts that [name] is currently [value]: the last value it was set to, since a property is
     * state and that is what a vendor holds. Earlier values do not count, so a property set to the
     * right value and then overwritten fails, and the failure lists the sequence.
     *
     * The overloads mirror `parameters { }`, so `assertPropertySet("seats", 3)` expects an
     * [AnalyticsValue.Int] and fails against a [AnalyticsValue.String] holding `"3"`.
     */
    public fun assertPropertySet(name: String, value: AnalyticsValue) {
        val history = properties.filter { it.name == name }
        when {
            history.isEmpty() -> fail("Expected property '$name' to be set, but it never was.")
            history.last().value != value -> fail(
                "Expected property '$name' to be ${value.describeTyped()}, " +
                    "but it was set to ${history.joinToString(" then ") { it.value.describeTyped() }}."
            )
        }
    }

    public fun assertPropertySet(name: String, value: String): Unit =
        assertPropertySet(name, AnalyticsValue.String(value))

    public fun assertPropertySet(name: String, value: Int): Unit =
        assertPropertySet(name, AnalyticsValue.Int(value))

    public fun assertPropertySet(name: String, value: Long): Unit =
        assertPropertySet(name, AnalyticsValue.Long(value))

    public fun assertPropertySet(name: String, value: Float): Unit =
        assertPropertySet(name, AnalyticsValue.Float(value))

    public fun assertPropertySet(name: String, value: Double): Unit =
        assertPropertySet(name, AnalyticsValue.Double(value))

    public fun assertPropertySet(name: String, value: Boolean): Unit =
        assertPropertySet(name, AnalyticsValue.Boolean(value))

    /**
     * Asserts that the user was identified as [userId] at some point. Unlike [assertPropertySet]
     * this is historical, not current: an `identify` followed by `reset` still passes. Use
     * [records] to assert on the order.
     */
    public fun assertIdentified(userId: String) {
        val identified = records.filterIsInstance<AnalyticsRecord.Identified>()
        if (identified.none { it.identity.userId == userId }) {
            fail("Expected the user to be identified as '$userId'.")
        }
    }

    private fun record(record: AnalyticsRecord) = synchronized(lock) {
        recorded += record
        Unit
    }

    private fun trackedIndicesOf(name: String): List<Int> = synchronized(lock) {
        recorded.withIndex()
            .filter { (_, record) -> record is AnalyticsRecord.Tracked && record.event.name == name }
            .map { (index, _) -> index }
    }

    private fun eventAt(index: Int): Event =
        (synchronized(lock) { recorded[index] } as AnalyticsRecord.Tracked).event

    /** Every failure carries the whole timeline. */
    private fun fail(message: String): Nothing {
        val timeline = records
            .takeIf { it.isNotEmpty() }
            ?.mapIndexed { index, record -> "  ${index + 1}. ${record.describe()}" }
            ?.joinToString(separator = "\n")
            ?: "  (nothing was recorded)"
        throw AssertionError("$message\n\nRecorded:\n$timeline")
    }
}
