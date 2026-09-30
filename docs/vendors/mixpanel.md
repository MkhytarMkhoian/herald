# Mixpanel

Herald's Mixpanel module sends events to Mixpanel with typed properties. It covers both places
where Mixpanel keeps attributes:

- the **people profile**, which describes a person;
- **super properties**, which are sent with every event.

| SDK | Package |
| --- | --- |
| Android | `io.github.mkhytarmkhoian:herald-mixpanel` |

## Setup

You create the `MixpanelAPI`: token, options, server URL for data residency. Herald never changes
them.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/vendors/MixpanelSetup.kt:provider"
    ```

## What reaches Mixpanel

| Herald | Mixpanel |
| --- | --- |
| an event | `trackMap(name, properties)`, with each value as its own JSON type |
| a `ScreenViewEvent` | `trackMap("screen_view")` with `screen_name`, sent by `ScreenViewMixpanelEventTrackerFactory` |
| a `UserProperty` | `people.set(name, value)`: the person's profile |
| any other property | a super property, sent with every later event |
| `identify` / `reset` | `identify(userId, true)` / `reset()` |
| `start` | `setEnableLogging(loggingEnabled)` |
| `flush` | `flush()` |
| `setEnabled(true)` / `setEnabled(false)` | `optInTracking()` / `flush()` then `optOutTracking()` |

## Factories

| Factory | Handles |
| --- | --- |
| `ScreenViewMixpanelEventTrackerFactory` | every `ScreenViewEvent`, as one `screen_view` event broken down by screen |
| `GenericMixpanelEventTrackerFactory` | everything else; usually last in the chain |
| `UserPropertyMixpanelPropertySetterFactory` | every `UserProperty` |
| `GenericMixpanelPropertySetterFactory` | every property, as a super property |
| `RequireMappedMixpanelEventTrackerFactory`, `RequireMappedMixpanelPropertySetterFactory` | nothing: throws for anything that reaches it |

!!! warning "Order the property chain"
    `UserPropertyMixpanelPropertySetterFactory` must come **before** the generic setter. A
    `UserProperty` is also a `Property`, so the generic setter would take it first, and the
    profile would never be written. A `UserProperty` goes to the profile only. To put a value in
    both places, write a factory that answers with both setters.

## Consent

**A fresh install collects** unless the `MixpanelAPI` is built opted out:
`MixpanelOptions.Builder().optOutTrackingDefault(true)`.

Herald can't opt out for you in `start()`. `optOutTracking()` deletes events not yet sent and the
stored user, so calling it on every launch would throw away the previous session and forget a user
who had already agreed. Once the user agrees, Mixpanel remembers it.

## Identity

Pass `identificationEnabled = false` to `MixpanelAnalyticsService` to never identify users in
Mixpanel.

## Watch out for

- **Mixpanel bills per event.** Screen views and high-frequency events add up. Drop what you don't
  need in Mixpanel's chain; [Routing](../guides/routing.md) shows how. [Moove](https://github.com/MkhytarMkhoian/Moove)
  drops screen views for Mixpanel entirely.
