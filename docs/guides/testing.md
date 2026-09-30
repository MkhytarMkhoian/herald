# Testing

Analytics breaks quietly. A renamed parameter, or a double tap that sends an event twice, shows up
weeks later as a wrong number on a dashboard. Herald's testing module lets you check it in tests.
Add `herald-testing`:

=== "Kotlin"

    ```kotlin
    testImplementation("io.github.mkhytarmkhoian:herald-testing:1.1.0")
    ```

=== "Groovy"

    ```groovy
    testImplementation 'io.github.mkhytarmkhoian:herald-testing:1.1.0'
    ```

The examples on this page are real tests, run on every Herald build.

## FakeAnalyticsProvider

`FakeAnalyticsProvider` is a fake vendor that records every call instead of sending it. It
implements all five interfaces, so you register it with a real `Herald`, and the test runs through
the same setup you ship: your factories, your wrappers, your dispatcher.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/testing/CheckoutAnalyticsTest.kt:fake-provider"
    ```

- **`assertTracked` expects exactly one match.** A double tap that sends an event twice fails the
  test instead of passing quietly. Use `assertTrackedTimes` when a repeat is expected.
- **Parameters are checked by type.** `"3"` sent as text fails against `param("seats", 3)`,
  because vendors receive the two differently.
- **`assertNothingElseTracked()`** fails on any event no earlier assertion mentioned. That's what
  catches a duplicate, or an event sneaking in from another feature.
- **A failure prints everything that was recorded,** which usually explains it:

  ```text
  Expected an event named 'checkout_pay_tapped', but it was never tracked.

  Recorded:
    1. property plan = pro
    2. event    checkout_pay_pressed { plan = pro }
  ```

The other assertions are `assertNotTracked`, `assertNothingTracked`, `assertPropertySet` and
`assertIdentified`. Assertions throw a plain `AssertionError`, so any test framework works.

## Order between calls

`records` holds every call in order: events, properties, sign-in, consent and start-up. Use it
when the order matters, such as a property that must be set before the event that should carry it:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/testing/CheckoutAnalyticsTest.kt:order"
    ```

## A lambda is enough for one interface

For a class that only tracks events, a lambda is a smaller fake and needs no extra dependency:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/testing/CheckoutAnalyticsTest.kt:lambda"
    ```

Use `FakeAnalyticsProvider` when the test uses several interfaces, checks the order of calls, or
runs through a real `Herald`.

## Testing a factory

A feature's factory takes an event and returns a `Resolution`, with nothing else involved, so you
can test it directly:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/testing/CheckoutAnalyticsTest.kt:factory"
    ```

To assert what a tracker sends, mock the vendor SDK and verify the call. Build real events rather
than mocked ones.

!!! note "Kotlin"
    With MockK, never stub a getter that returns an `AnalyticsValue`. It's a value class, and such
    stubs fail at random, depending on the JVM's state.
