package io.github.mkhytarmkhoian.herald.log.trackers

import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.log.AnalyticsLogger
import io.github.mkhytarmkhoian.herald.log.LogEventTracker
import io.github.mkhytarmkhoian.herald.log.logRecord

public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val logger: AnalyticsLogger,
) : LogEventTracker {

    override suspend fun track() {
        logger.log(
            logRecord(kind = "screen", headline = event.name, parameters = event.parameters)
        )
    }
}
