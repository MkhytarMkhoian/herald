package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.log.trackers.GenericEventTracker

/**
 * Prints any event with its name and parameters. Claims everything, so it belongs last in a chain.
 */
public class GenericLogEventTrackerFactory(
    private val logger: AnalyticsLogger,
) : LogEventTrackerFactory {

    override fun create(event: Event): Resolution<LogEventTracker> =
        Resolution.Claimed(GenericEventTracker(event, logger))
}
