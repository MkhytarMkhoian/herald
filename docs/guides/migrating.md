# Migrating an app

Most apps that adopt Herald already call a vendor SDK from many places, directly or through their
own wrapper. Moving everything at once is risky, so move one step at a time. You can release the
app after every step.

## 1. Put Herald in front of the old path

Add Herald with a single provider that passes every call to the wrapper you already have. Nothing
is sent differently yet; you've only added the new way in.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Migrating.kt:bridge"
    ```

Give `Herald` to your classes through its interfaces, as in
[Set up your app](../getting-started/app-setup.md). In debug builds, add the log provider to see
every call in Logcat.

## 2. Move call sites to events, feature by feature

Replace direct calls with an event class and `EventTrackerService`, one feature at a time:

=== "Kotlin"

    === "Before"

        ```kotlin
        class CheckoutViewModel(private val analytics: LegacyAnalytics) {
            fun onCheckout(plan: String, seats: Int) =
                analytics.logEvent("checkout_started", mapOf("plan" to plan, "seats" to "$seats"))
        }
        ```

    === "After"

        ```kotlin
        --8<-- "samples/QuickStart.kt:track"
        ```

Moved and unmoved code still go through the same wrapper, so dashboards don't change. Each moved
feature's tests can now use [`FakeAnalyticsProvider`](testing.md).

## 3. Move vendors to Herald's vendor modules

Once your code calls Herald, move vendors over one at a time: register the vendor's Herald module,
and remove that vendor from the old wrapper.

- **Start with the simplest vendor,** usually the one that gets every event, like Firebase. Its
  generic factory sends each event under its own name, as your wrapper did.
- **Compare before and after** with the log provider, or the vendor's own debug view.
- **Move special cases into factories as you go.** Tokens, special events and revenue each go to
  the feature that owns the event.

## 4. Consent and identity

Move sign-in, sign-out and the consent switch to `IdentifiableUserService` and `ConsentService`.
Check the [before-consent table](consent.md#before-consent-arrives): Herald's Adjust and AppsFlyer
modules keep those vendors quiet until consent, which may differ from what your wrapper did.

## 5. Remove the bridge

When the wrapper has no vendors left, delete it and the provider that called it. From then on,
adding or removing a vendor only changes the code that sets up Herald.
