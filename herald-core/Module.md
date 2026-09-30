# Module herald-core

The vocabulary and the fan-out. A plain Kotlin/JVM module with no Android, vendor or DI
dependency.

- Describe what happened with `Event` and `Property`, whose values are typed `AnalyticsValue`s.
- Depend on the capability you need: `EventTrackerService`, `PropertyTrackerService`,
  `IdentifiableUserService`, `AnalyticsLifecycleService` or `ConsentService`.
- Build one `Herald` where your app sets up its dependencies; it implements all five and forwards
  each call to every provider registered with it.
- Vendor adapters dispatch through chains of partial factories that answer with a `Resolution`.

Start with the [Android SDK page](https://mkhytarmkhoian.github.io/herald/sdks/android/) and the
[concepts](https://mkhytarmkhoian.github.io/herald/concepts/events-and-properties/).
