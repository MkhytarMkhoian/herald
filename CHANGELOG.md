# Change Log

## Version 1.1.0

_2026-09-30_

This release adds two vendors, AppsFlyer and Amplitude, and changes how you send revenue to Adjust.
Your events no longer implement a vendor's revenue type. Instead, the vendor's factory builds that
type from your event, so your events stay free of vendor code. See
[Revenue](https://mkhytarmkhoian.github.io/herald/guides/revenue/).

 * New: `herald-appsflyer` sends events to AppsFlyer, using AppsFlyer SDK 7. AppsFlyer keeps no user
   attributes, so this module handles events only.
 * New: AppsFlyer starts when the user gives consent. `start()` keeps the SDK stopped, and
   `setEnabled(true)` starts it, so a fresh install sends nothing, not even the install, before the
   user agrees.
 * New: `AppsFlyerPurchaseEvent` and `AppsFlyerSubscribeEvent` send `af_purchase` and `af_subscribe`
   with the amount under `af_revenue`. `AppsFlyerAdRevenueEvent` sends ad revenue through
   `logAdRevenue`.
 * New: `herald-amplitude` sends events to Amplitude, using the Amplitude Kotlin SDK. Parameters
   keep their type, properties become user properties, and a `ScreenViewEvent` becomes Amplitude's
   own `[Amplitude] Screen Viewed`.
 * New: `AmplitudeRevenueEvent` sends a purchase through Amplitude's revenue API. An optional
   `insertId` makes a purchase reported twice count once.
 * New: Amplitude consent uses Amplitude's opt-out, and `start()` waits for the SDK to finish
   setting up, so a failed setup is reported to you.
 * New: Each module's API reference opens with an overview of the module, and every class links to
   its source code at the release tag. This applies to the javadoc jars on Maven Central and to the
   new [website](https://mkhytarmkhoian.github.io/herald/).
 * Breaking: Adjust's revenue types are renamed and are now final data classes. `RevenueEvent` is
   `AdjustRevenueEvent`, and `AdRevenueEvent` is `AdjustAdRevenueEvent`. Your Adjust factory builds
   one from your event instead of your event implementing it.
 * Breaking: `AdjustRevenueEvent` is sent with the new `RevenueEventTracker`, which takes the
   token. `toAdjustEvent` no longer adds revenue to an event.
 * Breaking: `AdRevenueAdjustEventTrackerFactory` is removed. Your own Adjust factory now builds an
   `AdjustAdRevenueEvent` and passes it to `AdRevenueEventTracker`.

## Version 1.0.0

_2026-09-18_

The first release.

 * New: `herald-core` holds events, properties and `Herald` itself, with no Android, vendor SDK or
   DI library.
 * New: Five small interfaces for your classes: `EventTrackerService`, `PropertyTrackerService`,
   `IdentifiableUserService`, `AnalyticsLifecycleService` and `ConsentService`.
 * New: `Herald` passes each call to every vendor at the same time. A vendor that fails doesn't
   affect the others, and the failure goes to your `AnalyticsErrorReporter`.
 * New: Factories decide what each vendor receives, answering with a `Resolution`: `Claimed`,
   `Dropped` or `Declined`. The last factory in each vendor's chain decides what happens to events
   no other factory took.
 * New: Two marker types, `ScreenViewEvent` and `UserProperty`.
 * New: `herald-firebase` sends to Firebase Analytics, with typed parameters and GA4's own
   `screen_view`.
 * New: `herald-adjust` sends to Adjust (SDK v5): events by dashboard token, purchases with revenue,
   and ad revenue. A fresh install stays quiet until the user gives consent.
 * New: `herald-mixpanel` sends to Mixpanel, including the person's profile and super properties.
 * New: `herald-log` prints every call to Logcat, for debug builds.
 * New: `herald-compose` tracks from composables: `LocalEventTrackerService`, `TrackScreenView`,
   `TrackOnLifecycleEvent`, `rememberTracker` and `Modifier.trackImpression`.
 * New: `herald-testing` provides `FakeAnalyticsProvider`, which records every call so your tests
   can check them.
