# Compose

`herald-compose` is for tracking from composables that have no ViewModel to do it: a screen without
one, or a small component reused across screens. It needs no DI library.

=== "Kotlin"

    ```kotlin
    implementation("io.github.mkhytarmkhoian:herald-compose:1.1.0")
    ```

=== "Groovy"

    ```groovy
    implementation 'io.github.mkhytarmkhoian:herald-compose:1.1.0'
    ```

## Provide the tracker once

Your app provides an `EventTrackerService` once, at the top of its Compose tree:

```kotlin
--8<-- "samples/guides/ComposeSamples.kt:provide"
```

## The helpers

Every helper follows one rule: **track when the screen's lifecycle changes, or when something is
really on screen, never when a composable is composed.** Composition happens on rotation, when a
list item scrolls back into view, or when an `if` changes branch, and the user did none of those.
"Opened once" belongs in the ViewModel, and "navigated away" belongs to navigation. What a
composable knows best is "visible now".

| Helper | Fires | Answers |
| --- | --- | --- |
| `TrackScreenView(event, on = ON_RESUME)` | every `ON_RESUME`: first show, back navigation, returning from the background | how often is this screen looked at? |
| `TrackOnLifecycleEvent(event, on)` | every time the host lifecycle reaches `on` | the same, for any event: `ON_STOP` for "left the screen" |
| `rememberTracker()` | when you call the function it returns | a click in a small component with no ViewModel |
| `Modifier.trackImpression(event, threshold, minVisibleDuration)` | once per appearance, when that share of the layout has been visible that long | was this actually seen? |

### Screen views

```kotlin
--8<-- "samples/guides/ComposeSamples.kt:screen-view"
```

Wrap the event in `remember` keyed on its inputs, so it's the same event across recompositions.

### Clicks

```kotlin
--8<-- "samples/guides/ComposeSamples.kt:click"
```

### Impressions

```kotlin
--8<-- "samples/guides/ComposeSamples.kt:impression"
```

- **Once per appearance.** An item that scrolls out of a `LazyColumn` and back in counts again,
  which is what an impression is. "Each product once per session" is your app's rule, so it
  belongs in your code.
- **Visibility counts what every parent layout clips,** so an item half hidden under the app bar
  is half visible. A dialog drawn on top of the item doesn't count as hiding it.

## Previews

All four helpers do nothing in previews (`LocalInspectionMode`), so you can `@Preview` a screen or
component without providing a tracker.
