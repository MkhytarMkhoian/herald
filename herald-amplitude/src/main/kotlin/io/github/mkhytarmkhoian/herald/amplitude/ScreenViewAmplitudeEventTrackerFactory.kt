package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.amplitude.trackers.ScreenViewEventTracker

/**
 * Claims every [ScreenViewEvent] and declines everything else, which is
 * [GenericAmplitudeEventTrackerFactory]'s business.
 *
 * A screen view is sent as Amplitude's own `[Amplitude] Screen Viewed` event with the screen as
 * `[Amplitude] Screen Name` — the names Amplitude's autocapture uses — so it lands in the charts
 * Amplitude builds for screen views. Leave autocapture's screen views off when you use this
 * factory, or every screen is counted twice.
 *
 * To handle screen views differently, write a factory and place it before this one in the chain.
 */
public class ScreenViewAmplitudeEventTrackerFactory(
    private val amplitude: Amplitude,
) : AmplitudeEventTrackerFactory {

    override fun create(event: Event): Resolution<AmplitudeEventTracker> = when (event) {
        is ScreenViewEvent -> Resolution.Claimed(ScreenViewEventTracker(event, amplitude))
        else -> Resolution.Declined
    }
}
