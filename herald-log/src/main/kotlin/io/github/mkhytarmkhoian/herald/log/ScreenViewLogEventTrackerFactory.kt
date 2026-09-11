package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.log.trackers.ScreenViewEventTracker

/**
 * Claims every [ScreenViewEvent] and prints it as a screen record, headed by the screen name
 * rather than the event name. Declines everything else.
 */
public class ScreenViewLogEventTrackerFactory(
    private val logger: AnalyticsLogger,
) : LogEventTrackerFactory {

    override fun create(event: Event): Resolution<LogEventTracker> = when (event) {
        is ScreenViewEvent -> Resolution.Claimed(ScreenViewEventTracker(event, logger))
        else -> Resolution.Declined
    }
}
