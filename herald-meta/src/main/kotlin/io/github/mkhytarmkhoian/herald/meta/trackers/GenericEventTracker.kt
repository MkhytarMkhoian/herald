package io.github.mkhytarmkhoian.herald.meta.trackers

import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.meta.MetaEventTracker
import io.github.mkhytarmkhoian.herald.meta.toMetaParameters

/** Logs the event under its own name, with its parameters. */
public class GenericEventTracker(
    private val event: Event,
    private val logger: AppEventsLogger,
) : MetaEventTracker {

    override suspend fun track() {
        logger.logEvent(event.name, event.parameters.toMetaParameters())
    }
}
