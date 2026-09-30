# Identity

`IdentifiableUserService` tells every vendor who the user is at sign-in, so what's collected from
then on belongs to that user, and forgets them at sign-out.

=== "Kotlin"

    ```kotlin
    --8<-- "samples/guides/Identity.kt:session"
    ```

`Identity.userId` uses the name every vendor uses. Pass an id that doesn't change and doesn't
reveal who the person is, such as your backend's account id, never an email address or a phone
number.

## What each vendor does

| Vendor | `identify` | `reset` |
| --- | --- | --- |
| Firebase | `setUserId(userId)` | `setUserId(null)` |
| Mixpanel | `identify(userId, true)` | `reset()`: a new anonymous id |
| Adjust | a `user_id` global callback parameter; the key is configurable | removes that parameter |
| AppsFlyer | `setCustomerUserId(userId)` | `setCustomerUserId(null)` |
| Amplitude | `setUserId(userId)` | `reset()`: clears the user and rotates the device id |

If your app must not send a user id to Firebase or Mixpanel, pass `identificationEnabled = false`
to their service.

## Every cold start

Not every vendor remembers the user. AppsFlyer 7 forgets the user id between launches, so a
signed-in user who reopens the app would be anonymous there. At every start-up, identify a
signed-in user again, as `restore` above does.

The order matters for AppsFlyer. It attaches the user id to the launch it sends when it starts,
and with Herald that happens when consent is re-applied. So identify the user **before**
re-applying consent:

1. `start()`
2. `identify(...)`, if a user is signed in
3. `setEnabled(storedDecision)`
