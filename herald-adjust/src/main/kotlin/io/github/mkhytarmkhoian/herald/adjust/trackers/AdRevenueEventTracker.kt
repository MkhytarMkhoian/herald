package io.github.mkhytarmkhoian.herald.adjust.trackers

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.adjust.AdjustAdRevenueEvent
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTracker
import io.github.mkhytarmkhoian.herald.adjust.toAdjustAdRevenue

/** Sends one [AdjustAdRevenueEvent] through Adjust's ad-revenue API. No token is involved. */
public class AdRevenueEventTracker(
    private val event: AdjustAdRevenueEvent,
    private val adjust: AdjustInstance,
) : AdjustEventTracker {

    override suspend fun track() {
        adjust.trackAdRevenue(event.toAdjustAdRevenue())
    }
}
