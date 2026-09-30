# AppsFlyer

Herald's AppsFlyer module sends attribution and conversion events to AppsFlyer. AppsFlyer only gets
the events you choose: your conversions, under AppsFlyer's predefined names such as `af_purchase`
and `af_complete_registration`. It sends events only, because AppsFlyer keeps no
user attributes.

| SDK | Package |
| --- | --- |
| Android | `io.github.mkhytarmkhoian:herald-appsflyer` |

## Setup

AppsFlyer is initialised by your app in `Application.onCreate`, with the dev key and the
conversion and deep-link listeners. AppsFlyer 7 also ignores `start()` until a session-ready
listener is registered:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/vendors/AppsFlyerSetup.kt:init"
    ```

Then build the provider. It has no `properties`, because AppsFlyer keeps no user attributes, and
no generic factory at the end, so only the conversions a factory handles reach AppsFlyer:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/vendors/AppsFlyerSetup.kt:provider"
    ```

## What reaches AppsFlyer

| Herald | AppsFlyer |
| --- | --- |
| an event a factory handles | `logEvent(context, name, values)`, with each value as its own JSON type |
| an `AppsFlyerPurchaseEvent` | `af_purchase` with `af_revenue`, `af_currency` and optional content, quantity and order id |
| an `AppsFlyerSubscribeEvent` | `af_subscribe` with `af_revenue` and `af_currency` |
| an `AppsFlyerAdRevenueEvent` | `logAdRevenue(AFAdRevenueData, parameters)` |
| `identify` / `reset` | `setCustomerUserId(userId)` / `setCustomerUserId(null)` |
| `start` | `stop(true)`: stays silent |
| `setEnabled(true)` / `setEnabled(false)` | `stop(false)` then `start()` / `stop(true)` |

## Your conversions

Most AppsFlyer events are your own conversions under AppsFlyer's predefined names. Write a tracker
and a factory per event in the feature that owns it, as the
[factory chain](../concepts/factory-chain.md) describes. `AFInAppEventType` and
`AFInAppEventParameterName` hold the names.

For revenue, Herald's AppsFlyer module has its own types in AppsFlyer's terms, because AppsFlyer
counts revenue only under `af_revenue`. Your factory maps your event to one and hands it to the matching tracker:

| Type | Tracker |
| --- | --- |
| `AppsFlyerPurchaseEvent` | `PurchaseEventTracker` |
| `AppsFlyerSubscribeEvent` | `SubscribeEventTracker` |
| `AppsFlyerAdRevenueEvent` | `AdRevenueEventTracker` |

See [Revenue](../guides/revenue.md).

## Consent

**AppsFlyer starts on consent, not at start-up.** `start()` only stops the SDK, so a fresh install
sends nothing, not even the install. `setEnabled(true)` resumes it and calls `start()`, in the
order AppsFlyer requires after a stop. Don't call `start()` yourself.

AppsFlyer doesn't persist the stopped state, and `start()` applies it every launch, so re-apply
the stored decision after start-up. DMA consent (`setConsentData`) and `anonymizeUser` are set on
`AppsFlyerLib` directly.

## Identity

AppsFlyer 7 doesn't persist the customer user id between launches. Call `identify` on every cold
start for a signed-in user, **before** re-applying consent, so the launch AppsFlyer sends on
consent carries the id. See [Identity](../guides/identity.md#every-cold-start).

## Watch out for

!!! warning "Backup rules clash with yours"
    The AppsFlyer SDK declares its own `android:dataExtractionRules` and `android:fullBackupContent`.
    An app with its own rules fails the manifest merge. Add
    `tools:replace="android:dataExtractionRules,android:fullBackupContent"` to `<application>`,
    and copy AppsFlyer's exclusions into your rule files:

    ```xml
    <exclude domain="sharedpref" path="appsflyer-data"/>
    <exclude domain="sharedpref" path="appsflyer-purchase-data"/>
    <exclude domain="database" path="afpurchases.db"/>
    ```

    Without them, a restored backup reuses an old install's AppsFlyer identity.

- **`Context`.** AppsFlyer's API takes one, so Herald's AppsFlyer trackers and service do too.
  Pass the `Application`.
