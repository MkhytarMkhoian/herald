# Modular apps

In an app split into feature modules, each feature module should own its analytics: its events,
and how they reach each vendor. The app module sets up the vendors without knowing which features
exist. Factories are what make that possible.

## A feature ships its own factories

A feature module declares its events. It writes a [factory](../concepts/factory-chain.md) only for
a vendor that needs something special. An event that just needs sending under its own name needs
no factory at all, because the generic factory at the end of that vendor's chain sends it.

So you don't write one factory per feature for every vendor. A factory exists only where a vendor
really needs something different:

- tokens for Adjust;
- conversions for AppsFlyer;
- one of GA4's recommended events.

```text
:feature:checkout    CheckoutFirebaseEventTrackerFactory   (GA4 purchase)
                     CheckoutAdjustEventTrackerFactory     (dashboard tokens)
                     nothing for Amplitude: the generic factory is fine
:feature:profile     nothing at all: every event goes out under its own name
:feature:search      SearchMixpanelEventTrackerFactory     (a super property)
```

## DI collects them

The app module never imports a feature module to find its factories. Your DI library collects them:

=== "Kotlin"

    === "Koin"

        The feature adds its factory, named after the feature:

        ```kotlin
        --8<-- "samples/guides/ModularApps.kt:koin-feature"
        ```

        The analytics module builds the chain from whatever the features added:

        ```kotlin
        --8<-- "samples/guides/ModularApps.kt:koin-root"
        ```

        !!! warning "Name every factory you add"
            Koin keeps only one of two unnamed definitions of the same type: the module loaded last
            wins, and `getAll` finds just one factory. Give each one a `named(...)` qualifier, such
            as the feature's name.

    === "Hilt"

        The feature adds its factory with `@IntoSet`:

        ```kotlin
        --8<-- "samples/guides/ModularApps.kt:hilt-feature"
        ```

        The analytics module injects `Set<@JvmSuppressWildcards FirebaseEventTrackerFactory>` and
        builds the chain the same way.

## A DI set has no order

A chain uses the first factory that answers, but a DI set has no order. That's fine while each
feature's factories handle only that feature's events, which they should. Factories whose position
matters go around the injected set, placed by hand:

- Herald's screen-view factory, before the set;
- the generic factory, at the end;
- for properties, Mixpanel's `UserPropertyMixpanelPropertySetterFactory`, which must come before
  the generic one.

## A vendor that may be missing

A factory that takes a vendor object needs that object from DI too. If a vendor may be missing in
some builds, such as Firebase without a `google-services.json`, provide the vendor object as a
`single` that the app's setup code asks for only after checking the vendor exists. The feature's
factory is then never created when the vendor is missing.

## Two layouts

Implementing `FirebaseEventTrackerFactory` puts `herald-firebase` on a feature's classpath, so in
the layout above **feature modules know which vendors exist**. The upside is that an event and its
vendor rules live together. Some teams would rather features not know about vendors at all.

The other layout turns this around. Per-vendor modules own the vendor rules and depend on small API
modules that declare the events. Features depend only on `herald-core`:

```text
:analytics:firebase   depends on :feature:checkout:api, :feature:search:api
                      ships CheckoutFirebaseEventTrackerFactory, SearchFirebaseEventTrackerFactory
:feature:checkout     declares CheckoutStarted, and nothing about vendors
```

That's still not one big class that knows everything: it's one module per *vendor*, split into as
many small factories as it likes. The chain doesn't care where a factory was compiled. The real
choice is who owns the vendor rules: the feature team, or whoever owns analytics.

[Moove](https://github.com/MkhytarMkhoian/Moove) uses the first layout with Koin. Its `:analytics`
module depends on no feature, and features like `:tickets` and `:inspector` add their own factories.
