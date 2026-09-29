package io.github.mkhytarmkhoian.herald.adjust.trackers

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTracker
import io.github.mkhytarmkhoian.herald.adjust.AdjustRevenueEvent
import io.github.mkhytarmkhoian.herald.adjust.toAdjustEvent

/** Sends one [AdjustRevenueEvent] under [eventToken]: the tokened event, with the revenue attached. */
public class RevenueEventTracker(
    private val event: AdjustRevenueEvent,
    private val eventToken: String,
    private val adjust: AdjustInstance,
) : AdjustEventTracker {

    override suspend fun track() {
        adjust.trackEvent(event.toAdjustEvent(eventToken))
    }
}
