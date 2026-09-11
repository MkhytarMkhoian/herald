package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustEvent
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.asString

/**
 * One pending call to Adjust on behalf of one event.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface AdjustEventTracker {
    public suspend fun track()
}

/**
 * Builds the Adjust payload for an event under [eventToken], carrying the event's parameters as
 * callback parameters and, for a [RevenueEvent], its revenue. Public because a custom
 * [AdjustEventTracker] almost always needs it.
 */
public fun Event.toAdjustEvent(eventToken: String): AdjustEvent {
    val adjustEvent = AdjustEvent(eventToken)
    if (this is RevenueEvent) {
        adjustEvent.setRevenue(revenue, currency)
        deduplicationId?.let(adjustEvent::setDeduplicationId)
    }
    for ((key, value) in parameters) {
        adjustEvent.addCallbackParameter(key, value.asString)
    }
    return adjustEvent
}
