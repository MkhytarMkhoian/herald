# FAQ

## Why not just write our own wrapper?

Most teams do, and the wrapper grows into one class that must know every event and every vendor.
Herald is what that wrapper becomes when you take it seriously:

- small interfaces instead of one big class;
- each vendor's rules as factories that features add, without editing a shared file;
- consent and sign-in handled for each vendor;
- one vendor's failure never affecting the others, and a fake for tests.

[Philosophy](philosophy.md) explains each decision.

## How do I send an event to only one vendor?

An event is sent only by the vendors whose chain takes it. For vendors that only get chosen
events, like Adjust, simply don't write a factory for it. For vendors that get everything, like
Firebase, write a factory that takes it and answers `Dropped`. See [Routing](guides/routing.md).

## Why isn't my event reaching Adjust or AppsFlyer?

Both only get the events you choose:

- an Adjust event needs a token in `TokenAdjustEventTrackerFactory`;
- an AppsFlyer event needs a factory that takes it.

Check that a factory in that vendor's chain takes the event. For AppsFlyer, also check that consent
was given: AppsFlyer starts only then.

## Why is there no debug flag, or "strict mode" switch?

Because what a vendor receives is already decided by its chain, and what a debug build does is your
decision, not the library's. For a strict debug build, end the chains with each vendor's
required-mapping factory, such as `RequireMappedFirebaseEventTrackerFactory`, inside your own
`BuildConfig.DEBUG` check. Then every event nobody wrote a factory for is reported. See
[The factory chain](concepts/factory-chain.md#what-happens-to-events-without-a-factory).

## Does Herald queue or batch events when offline?

No. The vendor SDKs already do, each in its own way, and Herald passes events on to them. It adds
no storage, retries or network code of its own.

## Does tracking block the main thread?

No. Herald calls vendors off the main thread, all at the same time. See
[Threads](concepts/herald.md#threads).

## What if a vendor SDK throws?

Herald catches the error, and the other vendors still get the call. The error goes to your error
reporter with the vendor's name and what was happening, but never values or user ids. See
[Failures](concepts/herald.md#failures).

## Can one event reach one vendor as two calls?

Yes. A factory can answer with several trackers, for example sending a purchase event *and*
recording a charge on the buyer's Mixpanel profile. See
[One event, two vendor calls](concepts/factory-chain.md#one-event-two-vendor-calls).

## Do I need a DI framework?

No. Herald works with any DI library, or with none. The docs show Koin, Hilt, and wiring by hand.

## Can I call Herald from Java?

The Android SDK is written for Kotlin. Its calls are `suspend` functions, which are awkward to use
from Java. Call Herald from Kotlin, and wrap it if Java code needs it.

## Which platforms are supported?

Android today. A [Flutter SDK](sdks/flutter/index.md) is in development, with the same concepts and
the same vendors.

## Where do I report a bug or ask a question?

Open an issue on [GitHub](https://github.com/MkhytarMkhoian/herald/issues).
