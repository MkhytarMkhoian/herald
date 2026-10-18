package io.github.mkhytarmkhoian.herald.meta

import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.meta.trackers.GenericEventTracker

/**
 * Logs any event under its own name. Claims everything, so it only belongs last in a chain.
 *
 * Most apps leave it out and send Meta only their conversions, each claimed by a factory of its own.
 */
public class GenericMetaEventTrackerFactory(
    private val logger: AppEventsLogger,
) : MetaEventTrackerFactory, FallbackFactory {

    override fun create(event: Event): Resolution<MetaEventTracker> =
        Resolution.Claimed(GenericEventTracker(event, logger))
}
