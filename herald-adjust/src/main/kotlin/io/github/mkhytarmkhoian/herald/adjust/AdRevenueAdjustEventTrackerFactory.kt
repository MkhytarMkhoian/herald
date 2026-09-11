package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.trackers.AdRevenueEventTracker

/**
 * Claims every [AdRevenueEvent] and declines everything else.
 *
 * Ad revenue is the one Adjust event that needs no token, so this is the only factory besides
 * [TokenAdjustEventTrackerFactory] that Herald ships for Adjust. Put it before the token factory
 * so the chain reads as "ad revenue, then whatever has a token".
 */
public class AdRevenueAdjustEventTrackerFactory(
    private val adjust: AdjustInstance,
) : AdjustEventTrackerFactory {

    override fun create(event: Event): Resolution<AdjustEventTracker> = when (event) {
        is AdRevenueEvent -> Resolution.Claimed(AdRevenueEventTracker(event, adjust))
        else -> Resolution.Declined
    }
}
