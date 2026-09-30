# Log

The log provider prints every call to Logcat instead of sending it anywhere. Use it in debug builds
to see exactly what your app reports, and in what order: events, properties, sign-in, consent and
start-up.

| SDK | Package |
| --- | --- |
| Android | `io.github.mkhytarmkhoian:herald-log` |

## Setup

It writes through `AnalyticsLogger`, a one-method interface, so it depends on no logging library:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/QuickStart.kt:herald"
    ```

Register it only in debug builds, next to your real vendors.

## Output

```text title="Logcat"
D/analytics: [herald] start
D/analytics: [herald] enabled true
D/analytics: [herald] user    demo-user
D/analytics: [herald] event   fare_selected
D/analytics:     ├─ fare     = 1 Day Pass
D/analytics:     ├─ price    = 5.0
D/analytics:     └─ ryder_id = Adult
D/analytics: [herald] prop    tickets_purchased = 2
D/analytics: [herald] reset
```

Parameters are sorted by name and aligned. A screen view is headed by its screen name.

## Factories

The log provider has the same factories as a vendor module:

- `ScreenViewLogEventTrackerFactory`;
- `GenericLogEventTrackerFactory` and `GenericLogPropertySetterFactory`;
- `RequireMappedLogEventTrackerFactory` and `RequireMappedLogPropertySetterFactory`.

End its chain with `RequireMappedLogEventTrackerFactory`, and a debug build reports every event that
nobody mapped.

!!! warning "Debug builds only"
    The log prints the user id and every value as-is. That's the point in a debug build, and a
    leak in a release build.
