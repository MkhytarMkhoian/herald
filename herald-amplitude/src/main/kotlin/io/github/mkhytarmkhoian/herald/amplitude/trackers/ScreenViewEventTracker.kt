package io.github.mkhytarmkhoian.herald.amplitude.trackers

import com.amplitude.android.Amplitude
import com.amplitude.android.Constants
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.toAmplitudeProperties

public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val amplitude: Amplitude,
) : AmplitudeEventTracker {

    override suspend fun track() {
        val properties = event.parameters.toAmplitudeProperties() +
            (Constants.EventProperties.SCREEN_NAME to event.screenName)
        amplitude.track(Constants.EventTypes.SCREEN_VIEWED, properties)
    }
}
