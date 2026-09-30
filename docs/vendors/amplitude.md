# Amplitude

Herald's Amplitude module sends events to Amplitude with typed properties. Amplitude usually gets
every event:

- screen views use Amplitude's own `[Amplitude] Screen Viewed`;
- purchases go through Amplitude's revenue API.

| SDK | Package |
| --- | --- |
| Android | `io.github.mkhytarmkhoian:herald-amplitude` |

## Setup

You build the `Amplitude` instance: API key, server zone, autocapture and the rest of its
`Configuration`. Herald never changes them.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/vendors/AmplitudeSetup.kt:provider"
    ```

## What reaches Amplitude

| Herald | Amplitude |
| --- | --- |
| an event | `track(name, properties)`, with each value as its own JSON type |
| a `ScreenViewEvent` | `[Amplitude] Screen Viewed` with `[Amplitude] Screen Name` |
| an `AmplitudeRevenueEvent` | `revenue(Revenue)`, deduplicated by `insertId` when set |
| a property, including a `UserProperty` | `identify(Identify().set(name, value))`: a user property |
| `identify` / `reset` | `setUserId(userId)` / `reset()`, which also rotates the device id |
| `start` | waits for the SDK to finish setting up, so a failed setup is reported |
| `flush` | `flush()` |
| `setEnabled` | `optOut = !enabled` |

## Factories and revenue types

| Factory | Handles |
| --- | --- |
| `ScreenViewAmplitudeEventTrackerFactory` | every `ScreenViewEvent` |
| `GenericAmplitudeEventTrackerFactory` | everything else; usually last in the chain |
| `GenericAmplitudePropertySetterFactory` | every property |
| `RequireMappedAmplitudeEventTrackerFactory`, `RequireMappedAmplitudePropertySetterFactory` | nothing: throws for anything that reaches it |

For a purchase, your factory maps your event to `AmplitudeRevenueEvent` and hands it to
`RevenueEventTracker`; put that factory before the generic one. The type uses Amplitude's own
fields:

- `price`, `quantity`, `productId`, `revenueType`, `currency`;
- `revenue`, for a total that isn't `price × quantity`;
- `receipt` and `receiptSig`;
- `insertId`, passed on the call's options, since Amplitude's `Revenue` has no field for it.

See [Revenue](../guides/revenue.md).

## Consent

**A fresh install collects** unless the `Configuration` sets `optOut = true`. Amplitude doesn't
persist the opt-out, so re-apply the stored decision after start-up. Revoking stops new events;
events recorded before the revoke, while consent was given, may still upload.

## Watch out for

- **Autocapture and screen views.** Keep Amplitude's screen-view autocapture off while using
  `ScreenViewAmplitudeEventTrackerFactory`, or every screen is counted twice. The default
  autocapture setting is sessions only.
- **`reset()` rotates the device id,** so a signed-out user starts a new anonymous device in
  Amplitude.
