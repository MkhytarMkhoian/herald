package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.trackers.TokenEventTracker

/**
 * Sends the events named in [tokens] under the token each is mapped to, and declines everything
 * else.
 *
 * One token per event. For an event that needs several — the same action reported against two
 * partner campaigns, say — write a factory returning several [TokenEventTracker]s and put it
 * before this one in the chain.
 */
public class TokenAdjustEventTrackerFactory(
    private val tokens: Map<String, String>,
    private val adjust: AdjustInstance,
) : AdjustEventTrackerFactory {

    override fun create(event: Event): Resolution<AdjustEventTracker> =
        tokens[event.name]
            ?.let { token -> Resolution.Claimed(TokenEventTracker(event, token, adjust)) }
            ?: Resolution.Declined
}
