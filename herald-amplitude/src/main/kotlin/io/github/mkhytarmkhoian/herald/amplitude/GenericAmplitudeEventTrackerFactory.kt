package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.amplitude.trackers.GenericEventTracker

/**
 * Tracks any event under its own name with its parameters as event properties. Claims everything,
 * so it only belongs last in a chain — anything after it is unreachable.
 */
public class GenericAmplitudeEventTrackerFactory(
    private val amplitude: Amplitude,
) : AmplitudeEventTrackerFactory, FallbackFactory {

    override fun create(event: Event): Resolution<AmplitudeEventTracker> =
        Resolution.Claimed(GenericEventTracker(event, amplitude))
}
