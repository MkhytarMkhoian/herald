package io.github.mkhytarmkhoian.herald.log.trackers

import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.log.AnalyticsLogger
import io.github.mkhytarmkhoian.herald.log.LogEventTracker
import io.github.mkhytarmkhoian.herald.log.logRecord

/**
 * Prints the screen name rather than [ScreenViewEvent.name], which is what a vendor adapter sends.
 */
public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val logger: AnalyticsLogger,
) : LogEventTracker {

    override suspend fun track() {
        logger.log(
            logRecord(kind = "screen", headline = event.screenName, parameters = event.parameters)
        )
    }
}
