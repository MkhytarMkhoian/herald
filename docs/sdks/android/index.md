# Android SDK

Herald for Android is a set of Kotlin libraries on Maven Central under the group
`io.github.mkhytarmkhoian`:

- **the core:** events, properties and `Herald` itself;
- **one module per vendor;**
- **helpers** for Compose and for tests.

All modules share one version. Each vendor module pulls in `herald-core` and that vendor's SDK for
you.

[API reference](../../api/android/index.html){ .md-button } [Changelog](../../changelog/android.md){ .md-button } [Source](https://github.com/MkhytarMkhoian/herald){ .md-button }

## Installation

Here is every module. Keep the lines you need.

=== "Kotlin"

    ```kotlin
    dependencies {
        val herald = "1.1.0"

        // Always needed
        implementation("io.github.mkhytarmkhoian:herald-core:$herald")

        // One module for each analytics service you use
        implementation("io.github.mkhytarmkhoian:herald-firebase:$herald")
        implementation("io.github.mkhytarmkhoian:herald-adjust:$herald")
        implementation("io.github.mkhytarmkhoian:herald-mixpanel:$herald")
        implementation("io.github.mkhytarmkhoian:herald-appsflyer:$herald")
        implementation("io.github.mkhytarmkhoian:herald-amplitude:$herald")

        // Prints every event to Logcat, handy in debug builds
        implementation("io.github.mkhytarmkhoian:herald-log:$herald")

        // Tracking from Jetpack Compose
        implementation("io.github.mkhytarmkhoian:herald-compose:$herald")

        // A fake analytics service for your tests
        testImplementation("io.github.mkhytarmkhoian:herald-testing:$herald")
    }
    ```

=== "Groovy"

    ```groovy
    dependencies {
        def herald = '1.1.0'

        // Always needed
        implementation "io.github.mkhytarmkhoian:herald-core:$herald"

        // One module for each analytics service you use
        implementation "io.github.mkhytarmkhoian:herald-firebase:$herald"
        implementation "io.github.mkhytarmkhoian:herald-adjust:$herald"
        implementation "io.github.mkhytarmkhoian:herald-mixpanel:$herald"
        implementation "io.github.mkhytarmkhoian:herald-appsflyer:$herald"
        implementation "io.github.mkhytarmkhoian:herald-amplitude:$herald"

        // Prints every event to Logcat, handy in debug builds
        implementation "io.github.mkhytarmkhoian:herald-log:$herald"

        // Tracking from Jetpack Compose
        implementation "io.github.mkhytarmkhoian:herald-compose:$herald"

        // A fake analytics service for your tests
        testImplementation "io.github.mkhytarmkhoian:herald-testing:$herald"
    }
    ```

=== "Version catalog"

    ```toml
    # gradle/libs.versions.toml
    [versions]
    herald = "1.1.0"

    [libraries]
    herald-core = { module = "io.github.mkhytarmkhoian:herald-core", version.ref = "herald" }
    herald-firebase = { module = "io.github.mkhytarmkhoian:herald-firebase", version.ref = "herald" }
    herald-adjust = { module = "io.github.mkhytarmkhoian:herald-adjust", version.ref = "herald" }
    herald-mixpanel = { module = "io.github.mkhytarmkhoian:herald-mixpanel", version.ref = "herald" }
    herald-appsflyer = { module = "io.github.mkhytarmkhoian:herald-appsflyer", version.ref = "herald" }
    herald-amplitude = { module = "io.github.mkhytarmkhoian:herald-amplitude", version.ref = "herald" }
    herald-log = { module = "io.github.mkhytarmkhoian:herald-log", version.ref = "herald" }
    herald-compose = { module = "io.github.mkhytarmkhoian:herald-compose", version.ref = "herald" }
    herald-testing = { module = "io.github.mkhytarmkhoian:herald-testing", version.ref = "herald" }
    ```

    ```kotlin
    dependencies {
        implementation(libs.herald.core)

        // One module for each analytics service you use
        implementation(libs.herald.firebase)
        implementation(libs.herald.adjust)
        implementation(libs.herald.mixpanel)
        implementation(libs.herald.appsflyer)
        implementation(libs.herald.amplitude)

        implementation(libs.herald.log)
        implementation(libs.herald.compose)
        testImplementation(libs.herald.testing)
    }
    ```

## Modules

| Module | Use it for | Brings in |
| --- | --- | --- |
| `herald-core` | Events, properties and `Herald` itself. Feature modules that only describe or track events need nothing else. | kotlinx.coroutines |
| `herald-firebase` | [Firebase](../../vendors/firebase.md) Analytics (GA4). | Firebase BOM, `firebase-analytics` |
| `herald-adjust` | [Adjust](../../vendors/adjust.md): attribution, purchase and ad revenue. | Adjust SDK |
| `herald-mixpanel` | [Mixpanel](../../vendors/mixpanel.md): events, the people profile, super properties. | Mixpanel SDK |
| `herald-appsflyer` | [AppsFlyer](../../vendors/appsflyer.md): attribution, conversions, purchase and ad revenue. | AppsFlyer SDK |
| `herald-amplitude` | [Amplitude](../../vendors/amplitude.md): events, user properties, revenue. | Amplitude Kotlin SDK |
| `herald-log` | [Printing every call](../../vendors/log.md), in debug builds. | — |
| `herald-compose` | [Tracking from composables](compose.md). | Compose runtime and UI, Lifecycle Compose |
| `herald-testing` | [Asserting on what your app reports](../../guides/testing.md), in tests. | — |

Feature modules usually need only `herald-core`, plus the vendor module for each vendor they write
a [factory](../../concepts/factory-chain.md) for. The app module, which
[sets up Herald](../../getting-started/app-setup.md), depends on every vendor module.

## Compatibility

| Requirement | Version |
| --- | --- |
| Android `minSdk` | 23 |
| Android `compileSdk` | 36; 37 for `herald-compose` |
| Kotlin | 2.3 or newer. Herald is built with Kotlin 2.4, and a compiler reads metadata one minor version ahead. |
| Java bytecode | 17 |

Herald is built and tested against these vendor SDKs:

| SDK | Version |
| --- | --- |
| Firebase BOM | 34.19.0 |
| Adjust | 5.8.0 |
| Mixpanel | 8.11.0 |
| AppsFlyer | 7.0.1 |
| Amplitude Kotlin SDK | 1.33.0 |
| Compose | 1.12.1 |
| kotlinx.coroutines | 1.11.0 |

Each vendor module exposes its vendor's SDK as an `api` dependency. If your app declares a newer
version, Gradle uses the newer one, as it does for any dependency. A new major version of a vendor
SDK may need a Herald release.

## Kotlin specifics

The ideas are the same on every platform. On Android they look like this:

- **Calls are `suspend` functions.** Herald runs them on a coroutine dispatcher,
  `Dispatchers.Default` unless you choose another.
- **Parameter values are `AnalyticsValue`s,** a sealed interface whose variants are `@JvmInline`
  value classes, so they cost no extra object.
- **`EventTrackerService` and `PropertyTrackerService` are `fun interface`s,** so a fake in a unit
  test can be a lambda. For factories and trackers in your app, the docs use named classes, which
  are easier to find and test.

## R8 and ProGuard

Herald needs no keep rules of its own, and vendor SDKs bring their own. Herald's error messages
name classes, so in an obfuscated build they show obfuscated names; map them back with your mapping
file.

## Showcase

[Moove](https://github.com/MkhytarMkhoian/Moove) is an Android app that uses every module: feature
modules with Koin multibinding, consent, identity, Compose, and an in-app event inspector.
