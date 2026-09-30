# Events and properties

Your app sends Herald two kinds of data:

- an **event** is something that happened, like a tap or a purchase;
- a **property** is something that stays true about the user or the session until it changes,
  like a subscription plan or a theme.

Both are small classes your app declares, in its own words.

## Events

An `Event` has a `name` and, optionally, `parameters`. An event without parameters needs only its
name:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/Vocabulary.kt:no-parameters"
    ```

An event with details builds its parameters with the `parameters { }` builder:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/Vocabulary.kt:parameters"
    ```

Give the class and the event name the same meaning. Your code and your factories recognise an event
by its class, and vendors that simply log it see its name.

## Values keep their type

Every parameter and property value keeps its type: text, whole number, decimal or true/false. The
`parameters { }` builder takes the type from the value you pass, so you never name it yourself.

The type decides what a vendor's dashboard can do with the value, not just how it looks:

| Vendor | Receives |
| --- | --- |
| Firebase | whole numbers and decimals as numbers, so GA4 can sum and average them |
| Mixpanel, Amplitude, AppsFlyer | each value as its own JSON type |
| Adjust | text, because its callback parameters accept nothing else |

Whole numbers and decimals stay separate because they mean different things: `3` seats, a price of
`3.0`.

!!! note "Kotlin"
    Values are `AnalyticsValue`s: `String`, `Int`, `Long`, `Float`, `Double` or `Boolean`. Each is a
    value class, so it costs no extra object. A `Float` reaches every vendor as the decimal you
    wrote: `9.99f` arrives as `9.99`, not as `9.989999771118164`, which is what a plain conversion
    to `Double` would give.

!!! note "A number doesn't say what it means"
    To a vendor module, an `Int` called `amount` is just a number. When a vendor needs to know a
    value is revenue, your factory maps the event to that vendor's revenue type, which has a field
    for it. See [Revenue](../guides/revenue.md).

## Screen views

Herald has two marker types: interfaces that tell vendor modules what an event or property means.
`ScreenViewEvent` is the first. Every app has screens, and some vendors have a special event for
them:

- GA4's `screen_view`, with a `screen_name` parameter;
- Amplitude's `[Amplitude] Screen Viewed`.

Marking an event as a screen view lets those vendors receive it that way:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/Vocabulary.kt:screen-view"
    ```

`screenName` is separate from `name`. Vendors with a special screen view event put the screen name
in it; vendors without one log `name` like any other event.

## Properties

A `Property` is a name and a typed value that describes the session or the user, rather than one
moment. `UserProperty`, the second marker type, says the value belongs to the *person*:

=== "Kotlin"

    ```kotlin
    --8<-- "samples/concepts/Vocabulary.kt:properties"
    ```

That difference matters to vendors that store the two separately:

- **Mixpanel** saves a `UserProperty` on the person's profile, and any other property as a super
  property, sent with every later event;
- **Amplitude** only has user properties, so both kinds go there;
- **Firebase** and **Adjust** don't separate them, so both kinds are treated the same.

## Identity

Who the user is isn't a property. It's `Identity(userId)`, which you pass to
`IdentifiableUserService` at sign-in, because vendors have their own calls for it, such as
Firebase's `setUserId` and AppsFlyer's `setCustomerUserId`. See [Identity](../guides/identity.md).

## Next

[Capabilities](capabilities.md): how your classes send these.
