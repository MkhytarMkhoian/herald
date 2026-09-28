package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.amplitude.trackers.RevenueEventTracker

/**
 * Claims every [RevenueEvent] and declines everything else.
 *
 * Put it before [GenericAmplitudeEventTrackerFactory], which would otherwise send the purchase as
 * an ordinary event under its own name.
 */
public class RevenueAmplitudeEventTrackerFactory(
    private val amplitude: Amplitude,
) : AmplitudeEventTrackerFactory {

    override fun create(event: Event): Resolution<AmplitudeEventTracker> = when (event) {
        is RevenueEvent -> Resolution.Claimed(RevenueEventTracker(event, amplitude))
        else -> Resolution.Declined
    }
}
