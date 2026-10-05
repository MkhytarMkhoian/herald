package io.github.mkhytarmkhoian.herald.amplitude.trackers

import com.amplitude.android.Amplitude
import com.amplitude.android.Constants
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.toAmplitudeProperties

/**
 * Tracks [event] as Amplitude's own `[Amplitude] Screen Viewed`, with its name as
 * `[Amplitude] Screen Name`.
 *
 * Keep Amplitude's own screen-view autocapture off, or every screen is counted twice.
 *
 * Throws an [IllegalArgumentException] if the event has its own `[Amplitude] Screen Name` parameter,
 * because it would replace the screen's name.
 */
public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val amplitude: Amplitude,
) : AmplitudeEventTracker {

    override suspend fun track() {
        require(Constants.EventProperties.SCREEN_NAME !in event.parameters) {
            "Screen view '${event.name}' can't have a '${Constants.EventProperties.SCREEN_NAME}' " +
                "parameter: it holds the screen's name"
        }
        val properties = event.parameters.toAmplitudeProperties() +
            (Constants.EventProperties.SCREEN_NAME to event.name)
        amplitude.track(Constants.EventTypes.SCREEN_VIEWED, properties)
    }
}
