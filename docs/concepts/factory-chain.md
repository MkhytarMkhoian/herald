# The factory chain

For each vendor, an ordered list of small classes called factories decides what that vendor
receives for each event. Herald calls that list a **chain**. It's where a vendor's rules meet your
app's events.

## A factory answers only for its own events

A factory looks at one event and gives one of three answers, as a `Resolution`:

| Answer | Meaning | The chain then |
| --- | --- | --- |
| `Resolution.Claimed(trackers)` | "mine: send it with these trackers" | stops, and runs the trackers |
| `Resolution.Dropped` | "mine: send nothing" | stops, and sends nothing |
| `Resolution.Declined` | "not mine" | asks the next factory |

A factory answers only for the events it knows, and declines everything else. That's the whole
idea: no factory has to know every event in the app, so each feature can ship the factory for its
own events.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/FactoryChain.kt:factory"
    ```

- **`Claimed` takes one or more trackers,** the vendor calls to make. It can't be empty; use
  `Dropped` to take an event and send nothing.
- **A factory recognises an event with a plain `when` on its type,** so no DI container is
  involved.
- **Pass several trackers** to send one event to one vendor in more than one way. See
  [One event, two vendor calls](#one-event-two-vendor-calls).

## Trackers make the vendor call

A **tracker** is a small class that makes one vendor call for one event. Herald ships
generic ones, such as `GenericEventTracker`, which logs an event under its own name. When a vendor
needs something different, write your own:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/FactoryChain.kt:tracker"
    ```

A tracker has a single method, so it could be a lambda, but a named class is easier to test and
to find.

## One event, two vendor calls

Sometimes a vendor needs two calls for one thing that happened. Mixpanel is a good example: an
event shows up in its event reports, but its revenue reports read charges saved on the buyer's
profile. A purchase needs both.

Your event stays a normal event:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/SeveralTrackers.kt:event"
    ```

Herald's generic tracker already sends the event. For the charge, write a small tracker that calls
Mixpanel's profile API:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/SeveralTrackers.kt:tracker"
    ```

Then the Mixpanel factory takes the event and answers with both trackers:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/SeveralTrackers.kt:factory"
    ```

Herald runs both trackers one after the other, in that order, for this one vendor. If the first one
throws, the second doesn't run, and the failure goes to your error reporter. The other vendors are
unaffected:
Firebase and Amplitude still get `order_paid` from their own chains, as usual. The
[Moove](https://github.com/MkhytarMkhoian/Moove) sample app does the same for its ticket purchases.

## The chain

A chain is a composite factory: it asks its factories in order and returns the first answer that
isn't `Declined`:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/FactoryChain.kt:chain"
    ```

When every factory declines, the chain declines too, and the event isn't sent to that vendor.

## What happens to events without a factory

You don't write a factory for every event, so what happens to the others? You choose, per vendor,
with the last factory in its list:

- **Send it anyway.** End the list with the vendor's generic factory, and every event without a
  factory of its own goes out under its own name. That suits Firebase, Mixpanel and Amplitude,
  where you want everything.
- **Skip it.** End the list with nothing, and only the events a factory handled are sent. That
  suits Adjust and AppsFlyer, where each event needs setting up in the vendor's dashboard first.
- **Report it.** End the list with the vendor's required-mapping factory, such as
  `RequireMappedFirebaseEventTrackerFactory`, and an event nobody handled goes to your error
  reporter. Use it when every event must be sent on purpose, so a forgotten one shows up instead
  of disappearing.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/FactoryChain.kt:default"
    ```

The required-mapping version looks like this:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/FactoryChain.kt:strict"
    ```

!!! warning "A catch-all goes last"
    Generic and required-mapping factories answer for everything, so nothing after them is ever
    asked. They're marked `FallbackFactory`, and every composite checks when it's built. A
    fallback anywhere but last, or two of them, fails at start-up with the factory and its
    position named.

## Order matters

Apart from the last factory, the order is up to you:

- **Specific before general.** A feature's factory placed before Herald's screen-view factory can
  change how that feature's screen views are sent.
- **Mixpanel's properties** need `UserPropertyMixpanelPropertySetterFactory` before
  `GenericMixpanelPropertySetterFactory`. A `UserProperty` is also a `Property`, so the generic
  factory would otherwise claim it, and the people profile would never be written.
- **DI sets have no order.** When features add factories through a DI set, place the factories that
  must come first or last, like the screen-view and generic ones, around the set by hand. See
  [Modular apps](../guides/modular-apps.md).

## Properties have chains too

Properties work the same way: property setter factories answer with a `Resolution`, a composite
asks them in order, and a generic or required-mapping factory can close the list. `Dropped` keeps
one property away from one vendor.

## Next

- [Routing](../guides/routing.md): chains in practice, per vendor.
- [Custom markers](../guides/custom-markers.md): your own marker types, with their own vendor
  calls.
