package io.github.mkhytarmkhoian.herald.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.mkhytarmkhoian.herald.EventTrackerService

/**
 * The [EventTrackerService] used by [TrackScreenView], [TrackOnLifecycleEvent], [rememberTracker]
 * and [trackImpression].
 *
 * Provide it once, at the root of your Compose tree, from wherever your app gets its tracker:
 *
 * ```kotlin
 * setContent {
 *     CompositionLocalProvider(LocalEventTrackerService provides tracker) {
 *         App()
 *     }
 * }
 * ```
 *
 * Reading it without a provider throws. The helpers do not read it in previews or in the layout
 * inspector, so those work without a provider.
 */
public val LocalEventTrackerService: ProvidableCompositionLocal<EventTrackerService> =
    staticCompositionLocalOf {
        error(
            "No EventTrackerService provided. Wrap your Compose root in " +
                "CompositionLocalProvider(LocalEventTrackerService provides tracker) { ... }.",
        )
    }
