# Herald

> "A herald announces an event to whoever is listening"

A pluggable analytics library for Android. One core domain, one fan-out, and a separate module per
third-party service, so a consumer takes Firebase without dragging in Adjust or Mixpanel.

## Status

**1.0.0**, on Maven Central. The public API of `herald-core`, `herald-log` and `herald-testing` is
tracked by `apiCheck`; breaking changes come only with a major version.

## Modules

| Module | What it holds |
| --- | --- |
| `herald-core` | The domain and the fan-out. A plain Kotlin/JVM module — no Android, no vendor SDKs, no DI framework. |
| `herald-log` | Logging tracker. No third-party dependency. |
| `herald-firebase` | Firebase / GA4 adapter. |
| `herald-adjust` | Adjust adapter: token mapping, plus purchase and ad revenue. |
| `herald-mixpanel` | Mixpanel adapter, covering both stores: the people profile and super properties. |
| `herald-compose` | `LocalEventTrackerService`, `TrackScreenView` / `TrackOnLifecycleEvent`, `rememberTracker()` and `Modifier.trackImpression`, for composables that track without a ViewModel. No DI framework. |
| `herald-testing` | `FakeAnalyticsProvider` — a vendor that records instead of sending, for your tests. |

## Design

The vocabulary lives in `herald-core`, in the root package `io.github.mkhytarmkhoian.herald`, and
depends on nothing but the language: `Event` and `Property`, the `ScreenViewEvent` and
`UserProperty` markers, the four
capability interfaces (`EventTrackerService`, `PropertyTrackerService`, `IdentifiableUserService`,
`AnalyticsLifecycleService`, `ConsentService`), and `HeraldTrackerService`, which fans a call out to every registered
service.

A marker earns its place in `herald-core` only if it passes two tests: at least one adapter has to
call a **different vendor API** because of it, **and** the concept is universal to apps rather than
to a business domain. Exactly one event marker passes: `ScreenViewEvent`. Every app has screens,
and GA4 models a screen view as a *reserved* event whose parameter is the screen name, so the
adapter has no choice but to call it differently. `UserProperty` passes the same pair on the
property side — Mixpanel keeps a people profile separate from its super properties, and the marker
is what says which store an attribute belongs in.

Two instructive failures, both of which the README works through in full under *Extending it*:

**Revenue** passes the first test (Adjust has `setRevenue`, Firebase has `purchase`) and fails the
second. A vendor-neutral `RevenueEvent` could only carry the intersection of what the vendors want,
so any app doing real commerce would hit its ceiling and need a custom event anyway — and admitting
it would leave no principled reason to refuse the other thirty events in Google's recommended
taxonomy. So it lives in `herald-adjust` instead, in Adjust's own shape, next to the vendor that
demands it — see *Revenue* under *Extending it*.

**Structured naming** — identifying an event by where it happened, `screen/section/component/
element/action`, so that duplicate names across modules become impossible — fails *both*. No vendor
API changes because of it: an adapter still calls `logEvent`, just with a name assembled
differently. And a five-level taxonomy is one convention among many; Google recommends a different
one, and your next app may want four levels in another order. It is a genuinely good idea that
belongs to the app that chose it, along with the lossy decisions it forces — which fields become
the event name, which get dropped when a vendor caps parameters.

A marker that fails either test still works perfectly well; it just lives in your module rather
than in `herald-core`, and the chain is what makes that cost almost nothing.

Parameter and property values carry their type. `AnalyticsValue` has one variant per Kotlin type it holds —
`String`, `Int`, `Long`, `Float`, `Double`, `Boolean` — built with the `parameters` DSL, so no
event declaration has to name it:

```kotlin
override val parameters = parameters {
    put("plan", "pro")
    put("seats", 3)
    put("price", 9.99)
    put("trial", false)
}
```

The type decides what a dashboard can do with the value, not just how it prints. Firebase writes
integers with `putLong` and reals with `putDouble`, so GA4 can sum and average them where a number
spelled as text could only be grouped; Mixpanel receives each as its own JSON type. Vendors that
accept strings only — Adjust's callback parameters — flatten with `asString`.

A `Float` reaches Firebase as the decimal it was written as. Widening a float to a double exposes
its binary representation — `9.99f.toDouble()` is `9.989999771118164` — so `toBundle()` goes
through `Float.toString()` instead, and every adapter sends `9.99` for the same parameter.

`Property.value` is the same `AnalyticsValue`, for the same reason: Mixpanel stores a property as
JSON, so `total_purchases` written as an `Int` can answer "more than five" where the same value
spelled as text can only answer "exactly 42". Firebase user properties and Adjust session callback
parameters are string-only and flatten it, which costs them nothing.

```kotlin
class PurchaseCount(count: Int) : UserProperty {
    override val name = "total_purchases"
    override val value = AnalyticsValue.Int(count)
}
```

`Int` and `Double` are separate variants rather than one number, because the two say different
things about a quantity and survive to the vendor as `3` and `3.0` respectively.

Typing a value is not the same as naming its meaning. An `Int` called `amount` is still just a
number to an adapter, which is why revenue needs a marker interface rather than a typed parameter.

Each adapter dispatches through a **chain of partial factories**. A factory returns
`Resolution.Declined` to pass an event along, `Resolution.Claimed(...)` to claim it with the vendor
calls to make, or `Resolution.Dropped` to claim it and send nothing. Dispatch inside a factory is a
plain `when`, so no DI container is involved and none is required of a consumer.

```kotlin
CompositeFirebaseEventTrackerFactory(
    checkoutFirebaseFactory,                      // shipped by :feature:checkout
    profileFirebaseFactory,                       // shipped by :feature:profile
    ScreenViewFirebaseEventTrackerFactory(analytics), // GA4 reserved screen_view
    GenericFirebaseEventTrackerFactory(analytics),// catch-all — omit to drop unclaimed events
)
```

Partiality is the point: no single factory has to know the whole app's event vocabulary. A feature
module ships a factory for its own events and declines everything else, and the composition root
only concatenates the list — which is what DI multibinding already does well.

Order is the whole configuration, including what happens to an event nobody claimed. Put a factory
before Herald's to override how a marker is handled, and end the chain according to the policy you
want:

| Chain ends with | An unclaimed event |
| --- | --- |
| `GenericXEventTrackerFactory` | is sent under its own name |
| nothing | is ignored |
| `RequireMappedXEventTrackerFactory` | throws `UnhandledEventException` |

There is no flag for this and no default beyond the shape of the chain itself, so the policy is
always readable from the composition root — and it is chosen per vendor, which matters because
requiring every event to be mapped suits Firebase but not Adjust, where an event without a
dashboard token is expected to
go unclaimed.

Properties dispatch through their own chain, and Mixpanel is where the ordering earns its keep.
Mixpanel keeps two separate stores — the people profile and super properties — and `UserProperty`
is the marker that says which one an attribute belongs in:

```kotlin
CompositeMixpanelPropertySetterFactory(
    UserPropertyMixpanelPropertySetterFactory(mixpanel), // people.set — the person's profile
    GenericMixpanelPropertySetterFactory(mixpanel),      // super properties — catch-all
)
```

Swap those two lines and the profile is never written to: a `UserProperty` is also a `Property`, so
the catch-all claims it first. Firebase and Adjust need no such pair — neither separates an
attribute of the session from one of the person, so a single generic setter factory covers
`UserProperty` there too.

`herald-core` needs nothing from its host. Where an adapter does — `herald-log` has to print
somewhere — it asks through a one-method interface (`AnalyticsLogger`) rather than depending on a
logging library, so Herald never depends on a consumer's infrastructure.

The public API is explicit and checked. Every module compiles with `explicitApi()`, so a
declaration is API only because someone wrote `public`; and the binary-compatibility-validator
keeps a dump of that API in `<module>/api/*.api`, compared on every `check`. Between them, a symbol
cannot become public by accident and an existing one cannot change or vanish without appearing in a
pull request diff — including the changes that are source-compatible but binary-incompatible, such
as adding a defaulted parameter to `HeraldBuilder.provider`. When a change is intended, run
`./gradlew apiDump` and commit the result.

## Using it

Depend on the capability, not on the fan-out. A class that only records events takes an
`EventTrackerService`; one that only sets properties takes a `PropertyTrackerService`:

```kotlin
class CheckoutViewModel(private val analytics: EventTrackerService) {
    fun onPay() = viewModelScope.launch { analytics.track(PayTapped) }
}
```

`Herald` implements all four capabilities, so a consumer's container binds the one instance to
whichever of them its classes depend on. Nothing outside the composition root names the concrete
type, which is what lets the fan-out change without touching a single feature module.

The composition root is the one place that does name it:

```kotlin
val herald = Herald {
    provider(
        name = "firebase",
        events = firebaseTracker,
        properties = firebaseTracker,
        identity = firebaseService,
        lifecycle = firebaseService,
    )
    provider(name = "adjust", events = adjustTracker)
    dispatcher(Dispatchers.IO)   // optional; defaults to Dispatchers.Default
    errorReporter { provider, operation, failure -> crashlytics.recordException(failure) }
}
```

Herald forwards each call to every provider that has the matching capability, on the dispatcher you
give it — `Dispatchers.Default` unless you say otherwise, so a call from a ViewModel does not put
vendor SDK work on the main thread. Pass your own to put analytics on a dedicated thread, to share
one with the rest of the app, or to make it deterministic in tests with a `TestDispatcher`. Providers run **concurrently**, so a slow one delays only the caller rather than
every vendor behind it — the call still suspends until all of them are done, which keeps each
vendor's own ordering intact. A provider that throws is contained and reported, and the remaining
providers still run — analytics must not be able to break the app around it. Cancellation is the
exception: it propagates, because it is the caller's coroutine going away rather than a vendor
misbehaving.

Your `errorReporter` is called after the fan-out completes, one failure at a time and in provider
registration order, so it never needs to be thread-safe. If it throws, that is contained too and
each remaining failure is still reported — a crash reporter having a bad day must not become the
thing that crashes the app.

The `operation` handed to the reporter names what was in flight and, for a track or a property,
*which one* — `AnalyticsOperation.Track("checkout_started")` — so a single malformed event failing
among hundreds is findable rather than just "Firebase threw". It carries names only, never
parameters, property values or the identity id, so forwarding it straight to a crash reporter does
not copy personal data into a second vendor under a consent the user never gave for it.

Herald itself never decides what a vendor may receive: it forwards, and which events reach a vendor
is settled by that adapter's factory chain.
[Routing: which events reach which vendor](#routing-which-events-reach-which-vendor) explains how,
and how to keep one event out of one vendor.

One instance, four names — the interface a class asks for is just how much of Herald it is allowed
to see:

```kotlin
single { herald }                        // built once, at the composition root
single<EventTrackerService>       { get<Herald>() }
single<PropertyTrackerService>    { get<Herald>() }
single<IdentifiableUserService>   { get<Herald>() }
single<AnalyticsLifecycleService> { get<Herald>() }
single<ConsentService>            { get<Herald>() }
```

```
                 ┌─ EventTrackerService ────────→ feature ViewModels
   Herald ───────┼─ PropertyTrackerService ─────→ feature ViewModels
  (one object)   ├─ IdentifiableUserService ────→ login / logout
                 ├─ AnalyticsLifecycleService ──→ Application.onCreate
                 └─ ConsentService ─────────────→ privacy / consent screen
```

Consent is its own capability rather than a method on `AnalyticsLifecycleService`, because the two have
different callers: start-up runs once from the composition root, while consent is toggled from a
settings screen at any time. A screen that only records a consent decision should not be handed an
interface that can also re-initialise every vendor.

`ConsentService` is one boolean on purpose. It is the only consent signal every vendor implements
— Firebase's `setAnalyticsCollectionEnabled`, Mixpanel's `optInTracking`/`optOutTracking`, Adjust's
enable/disable. Anything more granular differs in shape per vendor (Firebase Consent Mode is a
four-key enum map, Adjust's is a partner-keyed bag, Mixpanel has none at all), so it is set on the
vendor SDK the consumer already owns, next to the API key and data residency.

### Before consent arrives

**Herald does not make a fresh install silent for you, except on Adjust.** Two of the three vendors
default to collecting, and their off switches are construction-time settings on objects you own —
not something an adapter can flip in `start()` without destroying data or overwriting your choice.
Set them where you build the SDK:

| Vendor | Fresh install collects? | Turn it off where |
| --- | --- | --- |
| **Adjust** | no — the adapter calls `disable()` before `initSdk()` in `start()` | already handled |
| **Firebase** | **yes** | `firebase_analytics_collection_enabled=false` in `AndroidManifest.xml` |
| **Mixpanel** | **yes** | `MixpanelOptions.Builder().optOutTrackingDefault(true)` on the instance you pass in |

Adjust is the exception because its flag is only a flag. Mixpanel's `optOutTracking()` deletes
unflushed events and clears the stored identity, so calling it every launch would discard the
previous session and de-identify a user who had already consented; Firebase's switch is read from
the manifest at initialisation, before any Herald code runs. Once consent is recorded,
`setEnabled(true)` covers all three.

Adjust also persists its flag, and `start()` overrides it on every launch — so re-apply the stored
decision after start-up rather than relying on Adjust to remember it.

Bind **only** `Herald` to those interfaces, never the adapters. `FirebaseAnalyticsTrackerService`
also satisfies `EventTrackerService`, so if it is bound too, a class can be injected with it and
quietly send events to Firebase alone — no compile error, no crash, just missing data everywhere
else.

Both are `fun interface`s, so a fake in a test is a lambda rather than a class or a mock:

```kotlin
val recorded = mutableListOf<Event>()
val analytics = EventTrackerService { recorded += it }
```

### Routing: which events reach which vendor

Routing is decided in one place: the vendor's **factory chain**. An event reaches a vendor when
some factory in that vendor's chain claims it, and not otherwise.

That keeps every decision about an event inside the module that owns it — the composition root
never learns your feature's events exist. Herald has no filtering layer of its own, on purpose.

#### What a factory returns

A factory is *partial* — it answers for the events it knows and passes on the rest. The return
type carries three distinct answers:

| Return | Meaning | The chain then |
| --- | --- | --- |
| `Resolution.Declined` | "not mine" | asks the next factory |
| `Resolution.Claimed(tracker)` | "mine — send these" | **stops**, and sends |
| `Resolution.Dropped` | "mine — send **nothing**" | **stops**, and sends nothing |

`Dropped` and `Declined` both send nothing here, and that is exactly why they are named rather than
encoded: this was once `List<T>?`, where `null` meant "not mine", an empty list meant "mine, send
nothing", and a populated list meant "mine, send this" — three meanings carried by the shape of a
nullable collection, with a name on none of them. `Claimed` rejects an empty list, so there is
exactly one way to say each thing.

The generic and `RequireMapped` factories answer for everything, so they go last — and only one
of them. They are marked `FallbackFactory`, and every composite checks when it is built: a
fallback anywhere but the end, or two of them, fails at start-up with the factory named and its
position. Nothing else about order is checked; "specific before general" among partial factories
is a decision you make in the chain.

`Claimed` takes handlers directly — `Claimed(tracker)`, `Claimed(one, two)`, `Claimed(*array)` —
or a `List` when you already have one.

Resolution is two lines. The composite takes the first answer that is not a decline:

```kotlin
factories.firstNotNullOfOrNull { it.create(event) }
```

and the tracker service sends whatever comes back:

```kotlin
eventTrackerFactory.create(event).orEmpty().forEach { it.track() }
```

So when **every** factory declines, the composite returns `Declined`, whose `handlers` are empty,
and the event is simply not sent. Falling off the end of the chain *is* the omission.

#### The terminator decides the default

The last factory in a chain sets the policy for events nobody claimed. `GenericFirebaseEventTrackerFactory`
never returns `Declined` — it claims everything — so placing it last means nothing can fall off the end:

| Chain ends with | An unclaimed event |
| --- | --- |
| `Generic…EventTrackerFactory` | is sent as-is |
| nothing | is **ignored** — this is what makes a vendor opt-in |
| `RequireMapped…EventTrackerFactory` | throws `UnhandledEventException` |

This is per vendor, which is usually what you want:

```kotlin
// Firebase is the warehouse: everything goes unless a factory says otherwise.
val firebaseEvents = CompositeFirebaseEventTrackerFactory(
    listOf(ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics)) +
        featureFirebaseFactories +
        GenericFirebaseEventTrackerFactory(firebaseAnalytics),   // terminator
)

// Adjust is opt-in: an event arrives only if some factory gave it a token.
val adjustEvents = CompositeAdjustEventTrackerFactory(
    TokenAdjustEventTrackerFactory(tokens, adjust),
)                                                                // no terminator, by design
```

Adjust ships no generic factory at all, so it behaves this way out of the box.

`RequireMapped…` has a consequence worth stating before you choose it: **every module that sets
a property (or tracks an event) has to answer for it in that vendor's chain — the app module
included.** A property added on the home screen with no Adjust factory is a contained failure in
the error reporter on every tap, not a silent no-op. The answer for "this one is not for that
vendor" is a factory returning `Resolution.Dropped`, which works for properties exactly as for
events:

```kotlin
class AppAdjustPropertySetterFactory : AdjustPropertySetterFactory {
    override fun create(property: Property) = when (property) {
        is PreferredSection -> Resolution.Dropped   // profile colour, not an attribution signal
        else -> Resolution.Declined
    }
}
```

#### Tracing one event

With the two chains above, `analytics.track(CheckoutStarted)`:

```
Firebase                                   Adjust
─────────────────────────────────────      ─────────────────────────────────────
Marker    is ScreenViewEvent? no  → null   Token   has a token?  no       → null
Checkout  is CheckoutStarted? yes          (end of chain)        → composite null
          → listOf(tracker)   STOP                               → orEmpty() → []
Generic   never reached                                          → nothing sent
→ logged to Firebase                       → not sent to Adjust
```

No marker interface and no central configuration were involved, and nothing outside
`:feature:checkout` mentions `CheckoutStarted`.

#### Omitting one event from a vendor that gets everything

If a vendor has a generic terminator, an event still falls to it unless something claims it first.
Claim it and return `Resolution.Dropped`:

```kotlin
// :feature:checkout — the only module that knows these events exist
internal class CheckoutFirebaseFactory(
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTrackerFactory {

    override fun create(event: Event): List<FirebaseEventTracker>? = when (event) {
        is PayTapped      -> Resolution.Claimed(GenericEventTracker(event, firebaseAnalytics))
        is CardNumberSeen -> Resolution.Dropped    // mine, and it goes nowhere
        else              -> Resolution.Declined   // not mine — let the chain continue
    }
}
```

Placed before the terminator, `CardNumberSeen` is claimed and dropped, and the generic factory
never sees it. The exclusion lives beside the event it excludes.

#### Keeping the composition root feature-agnostic

Have each feature contribute its factories to a set, so the root never names them:

```kotlin
// :feature:checkout
@Provides @IntoSet
fun checkoutFirebase(fa: FirebaseAnalytics): FirebaseEventTrackerFactory = CheckoutFirebaseFactory(fa)
```

```kotlin
// app — knows the injected set, not what is in it
CompositeFirebaseEventTrackerFactory(
    listOf(ScreenViewFirebaseEventTrackerFactory(fa)) +
        featureFactories.toList() +
        GenericFirebaseEventTrackerFactory(fa),
)
```

With Koin, `getAll<FirebaseEventTrackerFactory>()` does the same job.

> **Order matters, and a `Set` has none.** The composite is first-wins. That is harmless while
> feature factories claim disjoint events, but anything order-sensitive must be positioned
> explicitly around the injected set rather than left inside it — the marker factory, the
> terminator, and on the property side `UserPropertyMixpanelPropertySetterFactory`, which must come
> before `GenericMixpanelPropertySetterFactory` or the people profile is never written.

#### The escape hatch: decorate the provider

A provider need not be factory-based at all — `EventTrackerService` is a `fun interface`, so a
plain lambda is a valid provider, and it has no chain to route through. Anything the chain cannot
express is a decorator:

```kotlin
provider(name = "backend", events = EventTrackerService { if (it !is PiiEvent) backend.send(it) })
```

That is also where a runtime gate, sampling or debouncing belongs. Herald forwards; it does not
decide analytics semantics, so it deliberately offers no filtering of its own — an earlier
`eventFilter` parameter was removed once the chain made it redundant. Wrapping composes, applies to
any provider, and costs no API.

To gate the whole fan-out rather than one vendor, decorate `Herald` itself — it is an
`EventTrackerService` too.

## Extending it

Herald ships one event marker, one property marker, and no domain vocabulary. Everything else is
yours, and adding it takes a marker, a tracker per vendor that cares, and one entry in each chain.
Two worked examples follow: **revenue**, where a vendor API forces your hand, and **structured
naming**, where nothing forces it and you simply want a convention.

### Revenue: a marker because a vendor API demands one

Adjust attributes money to campaigns, and its SDK has two APIs for it that a plain event cannot
reach: `setRevenue(amount, currency)` on a tokened event, and `trackAdRevenue(AdjustAdRevenue)`
for ad impressions from a mediation SDK. A marker is the only way an adapter gets a typed `Double`
to hand to them — a *parameter* says what a value **is**, a marker says what the event **means**,
and revenue needs the second.

Neither marker is universal, so neither is in `herald-core`. They are in `herald-adjust`, in
Adjust's vocabulary:

```kotlin
// herald-adjust
interface RevenueEvent : Event {          // purchase revenue on a tokened event
    val revenue: Double
    val currency: String
    val deduplicationId: String? get() = null   // transaction id, so a retry counts once
}

interface AdRevenueEvent : Event {        // ad revenue; no token, its own Adjust call
    val source: String                    // "applovin_max_sdk", "admob_sdk", ...
    val revenue: Double
    val currency: String
    val adImpressionsCount: Int?  get() = null
    val adRevenueNetwork: String? get() = null
    val adRevenueUnit: String?    get() = null
    val adRevenuePlacement: String? get() = null
}
```

The two reach Adjust differently, and the difference is instructive. A `RevenueEvent` is still an
ordinary tokened event — the same `trackEvent` call with two more fields set — so there is no new
tracker or factory: `Event.toAdjustEvent` sets the revenue when it sees the marker, and the event
needs a token in `TokenAdjustEventTrackerFactory`'s map like any other. An `AdRevenueEvent` is a
*different* Adjust call with a different payload, so it gets its own tracker and factory:

```kotlin
CompositeAdjustEventTrackerFactory(
    AdRevenueAdjustEventTrackerFactory(adjust),   // claims AdRevenueEvent; no token needed
    TokenAdjustEventTrackerFactory(tokens, adjust), // everything with a token, revenue or not
)
```

Same call with more fields → the payload builder. Different call → a tracker. That rule is why
Firebase needs no revenue marker at all: GA4's `purchase` is an ordinary event whose `value` and
`currency` are parameters, and a typed `Double` parameter already arrives as a number:

```kotlin
data class SubscriptionPurchased(
    override val revenue: Double,
    override val currency: String,
    val plan: String,
) : RevenueEvent {
    override val name = FirebaseAnalytics.Event.PURCHASE
    override val parameters = parameters {
        put(FirebaseAnalytics.Param.VALUE, revenue)
        put(FirebaseAnalytics.Param.CURRENCY, currency)
        put("plan", plan)
    }
}
```

One event, three vendors: Firebase's generic factory logs `purchase` with typed parameters, Adjust
sends it under its token with revenue attached, and Mixpanel gets the parameters as JSON. Nothing
in `herald-core` changed, and the module that owns the event still owns its analytics.

The same shape works for a marker of your own — a typed interface, a tracker per vendor that
handles it differently, and one line in each chain before Herald's factories so yours wins:

```kotlin
val refundFirebaseFactory = FirebaseEventTrackerFactory { event ->
    if (event is RefundEvent) Resolution.Claimed(RefundFirebaseEventTracker(event, analytics))
    else Resolution.Declined
}
```

### Structured naming: a marker because *you* want a convention

Naming events by where they happened makes duplicate names across modules impossible, because the
name is assembled rather than typed. No vendor asks for this — which is exactly why it is yours and
not Herald's:

```kotlin
interface TreeStructureEvent : Event {
    val screen: String
    val section: String
    val component: String
    val element: String
    val action: String

    override val name: String
        get() = listOf(screen, section, component, element, action)
            .filter { it.isNotBlank() }
            .joinToString(separator = "_")
}

data class CheckoutPayTapped(
    override val screen: String = "checkout",
    override val section: String = "summary",
    override val component: String = "footer",
    override val element: String = "pay_button",
    override val action: String = "tap",
) : TreeStructureEvent
```

Because `name` is derived, an adapter that knows nothing about the marker already does the right
thing: `GenericFirebaseEventTrackerFactory` logs `checkout_summary_footer_pay_button_tap` with its
parameters, and you may need no tracker at all.

Write one only when you want the structure *spread across fields* instead of fused into the name —
and that is where the app has to make choices a library should not make for it:

```kotlin
class TreeStructureFirebaseEventTracker(
    private val event: TreeStructureEvent,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {
    override suspend fun track() {
        val bundle = event.parameters.toBundle().apply {
            putString("screen", event.screen)      // your call: keep it, or let the
            putString("section", event.section)    // event name carry it alone
            putString("element", event.element)
            putString("action", event.action)
        }
        firebaseAnalytics.logEvent(event.component, bundle) // or event.name — also your call
    }
}

val treeFirebaseFactory = FirebaseEventTrackerFactory { event ->
    if (event is TreeStructureEvent) listOf(TreeStructureFirebaseEventTracker(event, analytics))
    else null
}
```

GA4 caps events at 25 parameters and names at 40 characters, and the two lines commented above are
where an app trades one against the other. Those are the app's tradeoffs to make, per vendor, and
they are the reason this marker is documented here rather than shipped in `herald-core`.

## Tracking from Compose

`herald-compose` is for the composable that has no ViewModel to track through — a screen with
none, or a leaf component reused across screens. It takes no DI framework: the app provides the
tracker once at its Compose root, from wherever it gets it, and the helpers read it from there.

```kotlin
setContent {
    CompositionLocalProvider(LocalEventTrackerService provides tracker) {   // koinInject(), hiltViewModel(), a constructor...
        App()
    }
}

@Composable
fun MovieDetailsRoute(movieId: Long) {
    TrackScreenView(remember(movieId) { MovieDetailsScreenViewed(movieId) })
    ...
}
```

Four helpers, one rule: track on the *host lifecycle* or on what is *on screen*, never on composition. Entering or leaving
composition happens on rotation, on a list item scrolling back into view, on a branch flipping —
none of which the user did. "Opened once" belongs in the ViewModel; "navigated away" belongs to
navigation. What a composable can say better than either is "visible now".

| Helper | Fires | Answers |
| --- | --- | --- |
| `TrackScreenView(event, on = ON_RESUME)` | every `ON_RESUME`: first show, back navigation, foregrounding | how often is this looked at — what Firebase's automatic screen tracking counts |
| `TrackOnLifecycleEvent(event, on = ON_RESUME)` | every time the lifecycle reaches `on` | the same, for any event and any lifecycle event — `ON_STOP` for "left the screen", say |
| `rememberTracker(): (Event) -> Unit` | when you call it | a click in a leaf component with no ViewModel: `Button(onClick = { track(PromoBannerClicked(id)) })` |
| `Modifier.trackImpression(event, threshold = 0.5f, minVisibleDuration = ZERO)` | once per appearance, when that much of the layout is inside the window for that long | was this actually seen — list item impressions, ad viewability |

`trackImpression` fires once per appearance: an item that scrolls out of a `LazyColumn` and back in
counts again, which is what an impression is. "Each product once per session" is a business rule and
belongs to the caller. Visibility is after clipping by every ancestor, so half under the app bar is
half visible; a dialog drawn over the item does not clip it.

All four are inert under `LocalInspectionMode`, so a Route or a component can be `@Preview`ed
without providing a tracker.

## Testing an app that uses Herald

`herald-testing` publishes `FakeAnalyticsProvider`, a vendor that records instead of sending.
Because it implements all five capabilities, you register it as a real provider and the test then
runs through the composition root you actually ship — your factory chain, your decorators, your
dispatcher:

```kotlin
val analytics = FakeAnalyticsProvider()
val herald = Herald {
    provider(name = "test", events = analytics, properties = analytics)
}

viewModel.onPayTapped()

analytics.assertTracked("checkout_pay_tapped") { param("plan", "pro") }
analytics.assertNothingElseTracked()
```

`assertTracked` expects **exactly one** matching event, so a double-tap sending it twice fails
instead of passing quietly; use `assertTrackedTimes` when a repeat is intended. Parameters are
checked by type, so an `Int` sent as `"3"` fails against `param("seats", 3)` — the two reach a
vendor differently, and a test that cannot tell them apart is not testing what ships. Every failure
prints the whole timeline, which is usually what explains it:

```
Expected an event named 'checkout_pay_tapped', but it was never tracked.

Recorded:
  1. property plan = pro
  2. event    checkout_pay_pressed { plan = pro }
```

`assertNothingElseTracked()` is what turns a passing test into a meaningful one: it fails on any
event no earlier assertion named, so a duplicate or one leaking from another feature is caught.

For a class that only tracks events, a lambda is still the smaller fake and needs no dependency at
all:

```kotlin
val tracked = mutableListOf<Event>()
val events = EventTrackerService { tracked += it }
```

Reach for `FakeAnalyticsProvider` when the test spans more than one capability, cares about the
order between them — was the property set *before* the event meant to carry it? — or runs through a
real `Herald`. Everything it received is available in order as `records`.

## Where the pieces live

Composites are built **once per vendor, at the composition root**. Feature modules never build one
— they ship partial factories, and only for vendors that need something unusual for their events.
An event that just needs logging under its own name with its parameters needs no factory at all;
the generic terminator already handles it. So the cost is not "features × vendors", it is "only
where the mapping genuinely differs" — in practice Adjust, where every event needs a dashboard
token, plus a handful of Firebase and Mixpanel special cases.

```
:feature:checkout    CheckoutFirebaseFactory   (purchase mapping)
                     CheckoutAdjustFactory     (dashboard tokens)
                     — nothing for Mixpanel or log; generic is fine
:feature:profile     — nothing at all; every event goes generic
:feature:search      SearchMixpanelFactory     (needs super properties)

:app                 CompositeFirebaseEventTrackerFactory(
                         CheckoutFirebaseFactory,
                         ScreenViewFirebaseEventTrackerFactory(analytics),
                         GenericFirebaseEventTrackerFactory(analytics),
                     )
                     Herald { provider(…) }
```

### Two layouts, both supported

Implementing `FirebaseEventTrackerFactory` puts `herald-firebase` on a module's classpath, so the
layout above means **feature modules know which vendors exist**. That is good cohesion — an event
and its mapping live together — but some teams consider it a leak.

The alternative inverts the dependency. Per-vendor modules own the mappings and depend on the small
API modules that declare event types; features know only `herald-core`:

```
:analytics:firebase   depends on :feature:checkout:api, :feature:search:api
                      ships CheckoutFirebaseFactory, SearchFirebaseFactory
:feature:checkout     declares CheckoutStarted and nothing else
```

This is still not a god factory: it is one module per *vendor*, free to split into as many partial
factories as it likes. The chain does not care where a factory was compiled, only what order the
composite lists them in — so the choice is purely about whether a feature team or an analytics
owner should own the mapping.

### Collecting contributions through DI

The composition root should not import feature modules to find their factories; the DI container
collects them. With Koin that is `getAll<FirebaseEventTrackerFactory>()`, and two things learned
the hard way:

- **Qualify every contribution.** Two unqualified definitions of the same type in different
  modules *override* each other in Koin — the last module loaded silently wins and `getAll` sees
  one factory. Bind each as `factory<AdjustPropertySetterFactory>(named("tickets")) { … }`.
- **A factory that takes a vendor object needs that object bound too**, and a vendor that may be
  absent — Firebase without a `google-services.json` — is best bound as a `single` that the
  composition root only resolves after checking the vendor exists. The feature factory's
  definition is then never evaluated when the vendor is missing.

```kotlin
// :analytics — no feature module on its classpath
single<AdjustInstance> { Adjust.getDefaultInstance() }
single {
    Herald {
        val adjust = get<AdjustInstance>()
        val tracker = AdjustAnalyticsTrackerService(
            CompositeAdjustEventTrackerFactory(
                listOf(AdRevenueAdjustEventTrackerFactory(adjust)) + getAll<AdjustEventTrackerFactory>(),
            ),
            CompositeAdjustPropertySetterFactory(
                getAll<AdjustPropertySetterFactory>() + RequireMappedAdjustPropertySetterFactory,
            ),
        )
        provider(name = "adjust", events = tracker, properties = tracker, /* … */)
    }
}

// :feature:tickets
factory<AdjustEventTrackerFactory>(named("tickets")) { TicketsAdjustEventTrackerFactory(get()) }
```

## Coordinates

Group `io.github.mkhytarmkhoian`, artifact id per module (`herald-core`, `herald-firebase`, …),
root package `io.github.mkhytarmkhoian.herald`. Every module is published at the same version;
each adapter brings `herald-core` in transitively.

```kotlin
dependencies {
    implementation("io.github.mkhytarmkhoian:herald-core:1.0.0")
    implementation("io.github.mkhytarmkhoian:herald-firebase:1.0.0")
    testImplementation("io.github.mkhytarmkhoian:herald-testing:1.0.0")
}
```

## Licence

Apache-2.0. See [LICENSE](LICENSE).
