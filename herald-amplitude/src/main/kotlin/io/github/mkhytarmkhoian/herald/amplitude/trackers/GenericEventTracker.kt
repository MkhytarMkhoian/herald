package io.github.mkhytarmkhoian.herald.amplitude.trackers

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.toAmplitudeProperties

public class GenericEventTracker(
    private val event: Event,
    private val amplitude: Amplitude,
) : AmplitudeEventTracker {

    override suspend fun track() {
        amplitude.track(event.name, event.parameters.toAmplitudeProperties())
    }
}
