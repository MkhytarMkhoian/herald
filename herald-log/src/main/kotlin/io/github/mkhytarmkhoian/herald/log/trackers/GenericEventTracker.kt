package io.github.mkhytarmkhoian.herald.log.trackers

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.log.AnalyticsLogger
import io.github.mkhytarmkhoian.herald.log.LogEventTracker
import io.github.mkhytarmkhoian.herald.log.logRecord

public class GenericEventTracker(
    private val event: Event,
    private val logger: AnalyticsLogger,
) : LogEventTracker {

    override suspend fun track() {
        logger.log(logRecord(kind = "event", headline = event.name, parameters = event.parameters))
    }
}
