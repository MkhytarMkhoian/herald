# Set up your app

Your app sets Herald up in one place, usually your dependency injection setup and the Application
class. That place creates the vendors, builds one `Herald` from them, starts it, and hands it to the
rest of the app. No other code names a vendor or `Herald` itself.

## Build Herald with your vendors

Herald sends every call to each vendor you register. A registered vendor is called a
**provider**. Keeping each provider in its own small function keeps this part short:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/CompositionRoot.kt:herald"
    ```

Each of those functions looks like the Firebase one from the
[Quick start](quick-start.md#4-add-firebase), and each [vendor page](../vendors/firebase.md) shows
its own. A provider has a name and the parts of Herald that the vendor supports: events, user
properties, sign-in, start-up and consent. Leave out what a vendor doesn't have. AppsFlyer keeps no
user properties, for example, so its provider has no `properties`.

- **Pick a name you'd recognise in a crash log.** It's the name Herald reports when that vendor
  fails.
- **`providers(...)` takes vendors that are already built,** which is handy when your DI collects
  them from several modules. `provider(...)` builds one in place from its parts. You can use both in
  the same `Herald { }` block.

## Give it to your classes

Your classes never ask for `Herald` itself. Each one asks for the small interface it needs, and
you pass the same `Herald` for all of them:

| A class that… | asks for |
| --- | --- |
| tracks events | `EventTrackerService` |
| sets user properties | `PropertyTrackerService` |
| signs users in and out | `IdentifiableUserService` |
| starts or flushes analytics | `AnalyticsLifecycleService` |
| records the user's consent | `ConsentService` |

That keeps each class honest about what it does, and easy to test with a fake.

!!! warning "Bind only `Herald`, never a vendor's own tracker"
    Each vendor's tracker also implements `EventTrackerService`. If you bind one of those too, a
    class can end up with it and send events to that one vendor only. Nothing fails to compile and
    nothing crashes. The events are just missing everywhere else.

=== "Kotlin"

    === "Manual"

        ```kotlin
        --8<-- "samples/di/ManualSetup.kt:manual"
        ```

    === "Koin"

        ```kotlin
        --8<-- "samples/di/KoinSetup.kt:koin"
        ```

        Each vendor module adds its provider with `single<Herald.Provider>(named("firebase")) { … }`.
        Give each one a name like that: Koin keeps only one of two unnamed definitions of the same
        type, so `getAll` would find just one provider.

    === "Hilt"

        ```kotlin
        --8<-- "samples/di/HiltSetup.kt:hilt"
        ```

        Each vendor module adds its provider with `@Provides @IntoSet`.

## Start it

Call `start()` once, when the app starts, and then apply the consent decision the user made last
time. Some vendors forget it between launches, and some of Herald's vendor modules keep a vendor
quiet at start-up on purpose, until consent is applied (see [Consent](../guides/consent.md)).

Do it in `Application.onCreate()`, in a scope that lives as long as the app. That's where the
vendors' own setup guides initialise their SDKs, and it runs on every start of your app, including
ones without a screen, like a push notification or a WorkManager job:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/CompositionRoot.kt:start-from-application"
    ```

If a user is signed in, call `identify(...)` between the two calls. Some vendors forget the user
between launches, and AppsFlyer attaches the user id to the launch it sends when consent is
applied. See [Identity](../guides/identity.md#every-cold-start).

If your app uses use cases, these two calls fit in one, like `StartAnalyticsUseCase` in the
[Moove](https://github.com/MkhytarMkhoian/Moove) sample app.

An app with a single activity and no background work can do the same from that activity's
ViewModel instead. Moove does this: its main activity's ViewModel also handles deep links, so
nothing is tracked before the vendors have started.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/CompositionRoot.kt:start-from-entry"
    ```

## When a vendor fails

If a vendor's SDK throws, Herald catches the error and still sends to the other vendors, so
analytics never crashes your app. The downside is that a vendor failing on every call looks just
like one that works. That's why the example above passes an `errorReporter`: send the errors to
the crash or logging tool you already use.

=== "Kotlin"

    ```kotlin
    errorReporter { provider, operation, failure ->
        crashlytics.recordException(failure)
    }
    ```

- **`operation` says what failed:** `Track("checkout_started")`, `SetProperty("plan")`, `Identify`,
  `Start`, and so on. It holds names only, never parameter values or the user id, so it's safe to
  send to another tool.
- **Errors arrive one at a time,** after every vendor has finished, so your reporter doesn't need
  to be thread-safe.
- **If your reporter throws,** Herald catches that too.
- **Cancelling the caller isn't an error.** If the coroutine that called Herald is cancelled, the
  cancellation carries on as usual and nothing is reported.

## Threads

Herald never calls a vendor SDK on the main thread, so tracking from UI code is safe. It uses
`Dispatchers.Default` unless you pass another dispatcher:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/CompositionRoot.kt:dispatcher"
    ```

- **Vendors are called at the same time,** so one slow vendor doesn't hold up the others.
- **A call returns when every vendor has finished,** so two events always reach a vendor in the
  order you tracked them.
- **In tests,** pass a test dispatcher, such as `StandardTestDispatcher()`, so the calls run in a
  predictable order.

## Next

- [Philosophy](../philosophy.md): why Herald works the way it does.
- [Modular apps](../guides/modular-apps.md): feature modules that bring their own vendor rules.
