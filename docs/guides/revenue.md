# Revenue

Money is where vendors differ most. Each one counts revenue its own way, through its own API or
its own parameter. So Herald has no single revenue event that suits every vendor. Instead, each
vendor module has its own revenue type, in that vendor's terms, and you **map** your purchase to
it. [Philosophy](../philosophy.md#events-know-no-vendors) explains why.

## What each vendor needs

| Vendor | Purchase | Ad revenue |
| --- | --- | --- |
| Firebase | your event as it is, named GA4's `purchase` with `value` and `currency` parameters | your event as it is |
| Mixpanel | your event as it is; a charge on the person's profile needs a tracker of your own | your event as it is |
| Adjust | `AdjustRevenueEvent`, sent by `RevenueEventTracker`: the event under its token, with the amount and a transaction id | `AdjustAdRevenueEvent`, sent by `AdRevenueEventTracker` |
| AppsFlyer | `AppsFlyerPurchaseEvent`, sent by `PurchaseEventTracker` as `af_purchase` with `af_revenue`; for subscriptions, `AppsFlyerSubscribeEvent` and `SubscribeEventTracker` | `AppsFlyerAdRevenueEvent`, sent by `AdRevenueEventTracker` |
| Amplitude | `AmplitudeRevenueEvent`, sent by `RevenueEventTracker` through Amplitude's revenue API | your event as it is |

## The event

The event describes the purchase and knows nothing about the vendors:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Revenue.kt:purchase"
    ```

Firebase and Mixpanel send it through their generic factories, with no extra code.

## Mapping it for each vendor

Next to each vendor's factory, a small extension function turns your event into that vendor's
revenue type. Each type is a plain data class with the vendor's own field names, so the mapping is
a single constructor call.

`name = name` and `parameters = parameters` pass your event's name and parameters along. Adjust
sends the parameters as callback parameters, AppsFlyer as extra event values and Amplitude as
revenue properties. Leave `parameters` out to send none.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Revenue.kt:mappings"
    ```

Your event can't implement a vendor type, because those types are final classes. So only the
mapping knows the vendor exists.

**Counting a purchase once.** Pass your transaction id as Adjust's `deduplicationId`, AppsFlyer's
`orderId` and Amplitude's `insertId`. Then a purchase reported twice, for example by a retried
callback or a restored receipt, still counts once.

## The factories

Each vendor's factory takes your event, maps it, and hands the result to that vendor module's
tracker, which knows the vendor's API:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Revenue.kt:factories"
    ```

Put them before the generic factory in each vendor's chain. Otherwise Amplitude's generic factory
would take the purchase first and send it as an ordinary event. On Adjust, the factory passes the
token to `RevenueEventTracker` itself, so the event needs no entry in
`TokenAdjustEventTrackerFactory`.

## Ad revenue

An ad impression reported by your ad mediation SDK reaches Adjust and AppsFlyer through their
ad-revenue APIs, the same way:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Revenue.kt:ad-impression"
    ```

Both vendor modules call their tracker `AdRevenueEventTracker`, so a file that uses both imports
one under another name. Usually each lives in its own vendor's factory file, so it doesn't come up.

[Moove](https://github.com/MkhytarMkhoian/Moove)'s `TicketPurchased` reaches all five vendors this
way, and its simulated ad impression reaches both ad-revenue APIs.
