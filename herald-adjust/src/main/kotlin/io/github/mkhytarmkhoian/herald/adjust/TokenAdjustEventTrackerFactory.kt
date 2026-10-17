package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.trackers.TokenEventTracker

/**
 * Sends each event that has a token in [tokens] under that token, and declines everything else:
 *
 * ```
 * TokenAdjustEventTrackerFactory(listOf(AdjustToken<SignUpCompleted>("abc123")), adjust)
 * ```
 *
 * Events are matched by type, not by name, so a renamed event keeps its token. Throws an
 * [IllegalArgumentException] if a type has two tokens.
 *
 * One token per event. For an event that needs several, write a factory returning several
 * [TokenEventTracker]s and put it before this one in the chain.
 */
public class TokenAdjustEventTrackerFactory(
    tokens: List<AdjustToken<*>>,
    private val adjust: AdjustInstance,
) : AdjustEventTrackerFactory {

    private val tokens = tokens.toList()

    init {
        val repeated = this.tokens.groupBy { it.eventClass }.entries.firstOrNull { it.value.size > 1 }
        require(repeated == null) {
            "${repeated?.key?.simpleName} has two Adjust tokens. Give each event one token."
        }
    }

    override fun create(event: Event): Resolution<AdjustEventTracker> =
        tokens.firstOrNull { it.matches(event) }
            ?.let { Resolution.Claimed(TokenEventTracker(event, it.token, adjust)) }
            ?: Resolution.Declined
}
