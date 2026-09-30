# Philosophy

Most of Herald is decisions about what belongs in the library and what belongs to your app. This
page explains what Herald is built for, and how its design keeps those promises.

## The problem

Analytics usually gets messy around the second vendor:

- **Vendor calls spread everywhere.** `firebaseAnalytics.logEvent(...)` ends up in fifty
  ViewModels, and adding or removing a vendor means touching all of them.
- **Vendors want different things.** GA4 has its own `screen_view` event, Adjust only takes events
  with a dashboard token, and AppsFlyer counts money only as `af_revenue`. One `logEvent` call
  can't suit them all.
- **The usual wrapper turns into a monster.** An `AnalyticsManager` that translates every event
  for every vendor has to know every event in the app, so every new feature edits it.
- **The hard parts get skipped.** Consent, sign-in, a vendor crashing, threads and testing are
  handled differently by each vendor, so they're often not handled at all.

## What Herald is built for

**Your app doesn't know which vendors exist.** Feature code talks only to Herald's interfaces.
Vendor SDKs, their names and their rules stay behind Herald.

**Vendors change; your code and Herald's API don't.** When a vendor renames a field, adds an API
or changes its rules, the fix goes in that vendor's module, or in your factory for that vendor.
Your feature code stays the same, and so does Herald's core API. A vendor's needs are never a
reason for Herald's API to change.

**No central place that knows everything.** Each feature owns its events and decides how they
reach each vendor. Nothing in the app needs a list of every event.

**Every piece has one job** (the single responsibility principle). An event describes what
happened. A factory decides what one vendor receives. A tracker makes one vendor call. `Herald`
only passes calls on. Each class asks only for the interface it uses.

**Built for modular apps.** Feature modules plug their factories in through DI, so adding a feature
never means editing a shared file.

## How Herald keeps these promises

### Events belong to your app

Your events depend only on Herald's core library, which has no Android types, no vendor SDK and no
DI library. A feature can describe and track events without knowing which vendors the app uses.

### Events know no vendors

An event describes what happened, in your app's words, and nothing else. When a vendor wants the
data in its own format, a small mapping function turns your event into that vendor's type, and only
that vendor's factory uses it.

Say AppsFlyer should count ticket sales as revenue. The event stays plain:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/Philosophy.kt:event"
    ```

Everything AppsFlyer-specific lives in the tickets feature's AppsFlyer factory:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/Philosophy.kt:mapping"
    ```

If AppsFlyer changes how it counts revenue, only the mapping changes. If you stop using AppsFlyer,
you delete the factory and the mapping, and `TicketPurchased` and the code that tracks it don't
change. See [Revenue](guides/revenue.md) for the same event going to several vendors.

### Classes ask only for what they use

There's no `AnalyticsManager`. There are five small interfaces instead: one each for tracking
events, setting user properties, sign-in, start-up and consent. A ViewModel that tracks events asks
for `EventTrackerService` and can't do anything else. A consent screen can't restart every vendor
by accident, and a fake for a test is a one-line lambda.

### Each feature maps its own events

For each vendor, a short list of small factories decides what it receives. A factory handles the
events it knows and ignores the rest, so no factory needs to know the whole app. The checkout
feature ships the factory for checkout events, right next to them, and DI collects the factories
from every module.

### Each vendor's rules are code you can see

What a vendor receives, including what happens to events nobody wrote a factory for, is decided by
that vendor's list of factories. There's no hidden setting or global flag, so you see each vendor's
rules where you build its list. See
[What happens to events without a factory](concepts/factory-chain.md#what-happens-to-events-without-a-factory).

### Herald ships very few event types

Herald has only two special types, `ScreenViewEvent` and `UserProperty`. Every app has screens and
user attributes, and some vendors treat them differently. Anything more specific, like a purchase
or a sign-up, belongs to your app, because no single shape fits every business.

### Vendor names, vendor settings

Herald uses the names the analytics SDKs use, like `userId`, and the vendor types keep each
vendor's own field names. It also never configures a vendor SDK for you: API keys, servers, data
residency and log levels stay in your app, set on the SDK the way its docs describe.

### Numbers stay numbers

Parameters keep their type all the way to the vendor. A `3` arrives as a number, so dashboards can
sum it and compare it, rather than as the text `"3"`. Values become text only for vendors that
accept nothing else.

### Analytics never breaks the app

Herald calls vendors off the main thread and all at once. If one throws, Herald catches it, keeps
sending to the others, and reports the error to you, without any user data in the report.

### Forward, don't decide

Herald doesn't filter, sample or rate-limit. The factories decide what each vendor receives. If
you need more, wrap a vendor's tracker: `EventTrackerService` has a single method, so a gate or a
sampler is a few lines of your own code.

## What Herald doesn't do

- **No event catalogue.** Herald ships no `LoginEvent` or `SearchEvent`; your events are yours.
- **No queue or network of its own.** Vendor SDKs already store, batch and retry.
- **No automatic tracking.** Herald tracks what you tell it to. A vendor's own autocapture stays
  available on its SDK.
- **No remote configuration.** Which vendor gets what is code, in the factories.
- **No DI library.** Herald works with any of them, or none.

## Trade-offs

- **Feature modules that write factories depend on vendor modules.** The mapping sits next to the
  event, but the feature knows which vendors exist. If you'd rather it didn't, put the mappings in
  per-vendor modules instead. [Modular apps](guides/modular-apps.md) shows both layouts.
- **Order in a list matters, and a DI set has no order.** Factories that must come first, like the
  screen-view one, or last, like the generic one, are placed by hand around the injected set.
- **More small classes than one wrapper.** Each vendor has its own factories and trackers. In
  return, each one is small, easy to test and easy to replace.

## Next

- [Concepts](concepts/events-and-properties.md): each of these ideas in code.
- [Routing](guides/routing.md): the factories in practice.
