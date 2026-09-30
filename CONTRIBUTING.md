# Contributing

Thanks for helping with Herald. This page explains how to get a change ready to become part of the
library: a new vendor module, a change to an existing one, or a change to `herald-core`.

## Setting up

Herald builds with the Gradle wrapper and JDK 17 or newer. The Android modules need an Android SDK
(`ANDROID_HOME`, or `sdk.dir` in `local.properties`).

```bash
./gradlew build                 # compile, test, lint and API checks, every module
./gradlew publishToMavenLocal   # the artifacts Maven Central receives, into ~/.m2
```

To try your change in an app, publish it under a version of its own, so it can't be confused with
a release, and depend on that version from `mavenLocal()`:

```bash
./gradlew publishToMavenLocal -Pversion=1.1.0-SNAPSHOT
```

[Moove](https://github.com/MkhytarMkhoian/Moove) is a sample app that uses Herald this way, and a
good place to see your change working.

## What every change needs

- **Code that matches its surroundings.** There's no formatter. Use the same naming and the same
  amount of comments as the code around your change, and keep lines within 100 columns. Name things
  the way the analytics vendors do, for example `Identity.userId`, because every SDK calls it a user
  id.
- **KDoc that says what a declaration does** and why a caller needs to know, not how the code got
  that way.
- **Tests,** in the module's own `src/test`, with JUnit 4, [MockK](https://mockk.io) and
  `kotlinx-coroutines-test`. `herald-testing` is a module for apps' tests, not for Herald's own.
- **A careful public API.** Every module compiles in explicit API mode, so nothing is public unless
  you write `public`. Anything public is part of Herald's API and can only change incompatibly in a
  major version.
- **Documentation, when users would notice.** Update the matching page in `docs/`, and its example
  in `docs-samples`, which the build compiles. Write in plain, simple words.
- **A line in `CHANGELOG.md`** under the next, unreleased version, starting with `New:`, `Fix:`,
  `Upgrade:` or `Breaking:`.

**One MockK rule:** never stub a getter that returns an `AnalyticsValue`
(`every { property.value } returns …`). It's a value class, and the stub fails at random, depending
on the JVM's state. Build a real object instead, as `herald-mixpanel`'s `TestProperties.kt` does.
After changing such tests, run `./gradlew build --rerun-tasks`: a cached passing build proves
nothing here.

## A new vendor module

A new vendor gets a module of its own, shaped like the existing ones. Use `herald-mixpanel` as the
template, or `herald-appsflyer` for a vendor that only takes events. Everything specific to the
vendor stays in its module; a new vendor never needs a change to `herald-core`.

- [ ] **The module.** Apply the `herald.android.library` and `herald.publish` plugins. Depend on
      the vendor SDK with `api`, since its types appear in constructors. Add the module to
      `settings.gradle.kts`.
- [ ] **The vendor's settings stay the app's.** The module takes an SDK object the app has already
      configured, and never sets keys, endpoints, data residency or log levels itself.
- [ ] **Services.** A `<Vendor>AnalyticsTrackerService` for events, and for properties if the vendor
      keeps user attributes. A `<Vendor>AnalyticsService` for start-up, sign-in and consent.
- [ ] **Factories:**
    - [ ] the factory `fun interface`s, and `Composite<Vendor>EventTrackerFactory` and
          `Composite<Vendor>PropertySetterFactory`, which call `requireFallbackLast`;
    - [ ] a generic factory marked `FallbackFactory`, if sending an event under its own name makes
          sense for this vendor;
    - [ ] `RequireMapped<Vendor>EventTrackerFactory` and `RequireMapped<Vendor>PropertySetterFactory`
          objects;
    - [ ] one tracker class for each different vendor call.
- [ ] **How consent works, backed by the vendor's own documentation.** What does a fresh install
      send? What does `setEnabled` call? Does the vendor remember the choice across launches?
- [ ] **Typed values.** Send numbers and booleans as themselves wherever the vendor accepts them.
- [ ] **Tests** for every service method, factory and tracker.
- [ ] **Everywhere the module is listed:**
    - [ ] `unvalidatedByDesign` in the root `build.gradle.kts`;
    - [ ] a `Module.md` of its own, for the API reference;
    - [ ] a vendor page under `docs/vendors/`, and in the `mkdocs.yml` navigation;
    - [ ] the modules table in `README.md`;
    - [ ] the modules table and both install lists on `docs/sdks/android/index.md`;
    - [ ] `CHANGELOG.md`.

## A change to an existing vendor module

**A new vendor call,** such as a new API the vendor added. Write one tracker class for it and a
factory that uses it, and test what the tracker sends to the vendor SDK.

**A vendor type,** only where a vendor API needs one, such as revenue. Make it a final data class
that implements `Event`, named after the vendor (`<Vendor>RevenueEvent`), with the vendor's own
field names and a `parameters` field. Apps map their events to it; an app event never implements
one.

**A vendor SDK upgrade:**

- [ ] Update the version in `gradle/libs.versions.toml`.
- [ ] Read the vendor's release notes, and check that start-up, consent and sign-in still behave as
      the vendor page describes.
- [ ] Update the vendor SDK table on `docs/sdks/android/index.md`.
- [ ] Add an `Upgrade:` line to `CHANGELOG.md`.

## A change to `herald-core`

`herald-core` is the part every app and every vendor module depends on, so a change there has to
work for all vendors:

- **Nothing vendor-specific.** If only one vendor needs it, it belongs in that vendor's module.
- **No Android types, no vendor SDK and no DI library.**
- **Its public API is recorded** in `herald-core/api/herald-core.api` and compared on every build.
  When you change the API on purpose, run `./gradlew apiDump` on its own and include the updated
  file in your change. The same applies to `herald-log` and `herald-testing`. The other modules'
  `api` files are frozen snapshots; leave them alone.

## Before you open a pull request

- [ ] `./gradlew build` passes.
- [ ] `scripts/build_docs.sh` passes, if you changed docs, samples or KDoc.
- [ ] A public API change has its updated `api` file, and a breaking change is marked `Breaking:`
      in `CHANGELOG.md`.
- [ ] `CHANGELOG.md` describes anything a user of Herald would notice.
