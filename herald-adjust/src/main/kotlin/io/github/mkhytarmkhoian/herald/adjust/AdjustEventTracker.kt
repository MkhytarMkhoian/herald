package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustEvent
import io.github.mkhytarmkhoian.herald.AnalyticsValue
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
 * callback parameters. Public because a custom [AdjustEventTracker] almost always needs it.
 */
public fun Event.toAdjustEvent(eventToken: String): AdjustEvent = parameters.toAdjustEvent(eventToken)

internal fun Map<String, AnalyticsValue>.toAdjustEvent(eventToken: String): AdjustEvent {
    val adjustEvent = AdjustEvent(eventToken)
    for ((key, value) in this) {
        adjustEvent.addCallbackParameter(key, value.asString)
    }
    return adjustEvent
}
