# Custom providers

A provider is anything that implements one or more of Herald's five interfaces. Herald's vendor
modules build theirs from factory chains, but a provider can be much simpler.

## Your own backend

To send events to your own collection endpoint, implement `EventTrackerService`:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/CustomProvider.kt:backend"
    ```

Register it like any vendor, with only the interfaces it implements. It's called at the same time
as the others, and its failures are caught and reported like theirs.

## Wrapping a provider

`EventTrackerService` has a single method, so wrapping one takes a few lines. That's the place for
rules that depend on the situation rather than on the event: sampling, a runtime switch, skipping
repeated events, keeping personal data on the device.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/CustomProvider.kt:decorators"
    ```

Then register the wrapped tracker instead of the original:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/CustomProvider.kt:register"
    ```

Herald has no filtering or sampling of its own, on purpose. Wrappers can be combined, work with any
provider, and need nothing new from Herald.

## A full vendor module

For a vendor you'll use seriously, with its own types, consent and identity, write a module shaped
like Herald's own:

- factory interfaces, and composites that check the generic factory comes last;
- a generic factory and a required-mapping factory, like `GenericFirebaseEventTrackerFactory` and
  `RequireMappedFirebaseEventTrackerFactory`;
- one tracker per distinct vendor call;
- services for lifecycle, identity and consent.

The [new vendor module](../contributing.md#a-new-vendor-module) checklist lists every
piece. It's written for vendor modules added to Herald, but works the same for one that lives in
your app.
