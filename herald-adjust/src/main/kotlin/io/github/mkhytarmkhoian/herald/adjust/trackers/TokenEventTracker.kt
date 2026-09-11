package io.github.mkhytarmkhoian.herald.adjust.trackers

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTracker
import io.github.mkhytarmkhoian.herald.adjust.toAdjustEvent

/**
 * Sends one event to Adjust under one token.
 *
 * There is no generic counterpart to this, because Adjust has no generic path: an event only
 * exists there if it has a token issued in the Adjust dashboard. An event with two tokens is two
 * of these.
 */
public class TokenEventTracker(
    private val event: Event,
    private val eventToken: String,
    private val adjust: AdjustInstance,
) : AdjustEventTracker {

    override suspend fun track() {
        adjust.trackEvent(event.toAdjustEvent(eventToken))
    }
}
