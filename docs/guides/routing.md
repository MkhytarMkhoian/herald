# Routing

Which events reach which vendor is decided in one place: each vendor's
[factory chain](../concepts/factory-chain.md). An event reaches a vendor when a factory in that
vendor's chain takes it, and not otherwise. `Herald` itself never filters.

So every routing decision sits next to the events it's about, and the code that sets up Herald
never needs to know what your features track.

## Two kinds of vendor

Most apps use vendors in two ways, and the end of each chain is what makes the difference:

- **Vendors that get everything,** for product analytics: usually Firebase, Amplitude or Mixpanel.
  Their chain ends with the generic factory, which sends any event under its own name.
- **Vendors that get a few chosen events,** usually attribution and marketing vendors such as
  Adjust and AppsFlyer. Their chain ends with nothing, so an event no factory took isn't sent.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Routing.kt:two-kinds"
    ```

Adjust has no generic factory at all, because an Adjust event only exists with a token from the
Adjust dashboard. So Adjust always works the second way.

## Following one event

With those two chains, `analytics.track(CheckoutStarted)` goes like this:

| Firebase chain | Adjust chain |
| --- | --- |
| feature factories: `CheckoutStarted` isn't theirs, so each declines | `TokenAdjustEventTrackerFactory`: no token for `checkout_started`, so it declines |
| `ScreenViewFirebaseEventTrackerFactory`: not a screen view, so it declines | end of the chain, so it's **not sent** |
| `GenericFirebaseEventTrackerFactory`: takes it, and it's **sent under its own name** | |

No shared configuration was involved, and nothing outside the checkout feature mentions
`CheckoutStarted`.

## Keeping one event away from one vendor

In a chain that ends with the generic factory, every event is sent unless a factory takes it first.
To keep one out, take it and answer `Resolution.Dropped`, in the factory of the feature that owns
the event:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/FactoryChain.kt:factory"
    ```

`CardNumberSeen` is dropped, so the generic factory never sees it. The rule lives right beside the
event.

Properties work the same way:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Routing.kt:drop-property"
    ```

## Requiring every event to be mapped

If a chain ends with the vendor's required-mapping factory, such as
`RequireMappedAdjustEventTrackerFactory`, every event no factory took is reported to your error
reporter as an `UnhandledEventException`. Use it for a vendor where every event must be sent on
purpose.

Know what it costs first. **Every module that tracks an event or sets a property must then answer
for it in that vendor's chain**, the app module included. A property set on the home screen with no
factory is reported as a failure on every call, instead of being quietly skipped. To say "this one
isn't for that vendor", add a factory that answers `Dropped`.

## When a factory can't express it

Factories route by *event*. To route by *situation*, such as a runtime flag, sampling or a group of
users, wrap the vendor's tracker instead. See
[Custom providers](custom-provider.md#wrapping-a-provider).

To apply a rule to every vendor at once, wrap `Herald` itself: it's an `EventTrackerService` too.
