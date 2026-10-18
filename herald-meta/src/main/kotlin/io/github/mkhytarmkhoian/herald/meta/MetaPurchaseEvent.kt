package io.github.mkhytarmkhoian.herald.meta

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event

/**
 * A purchase as Meta's standard `fb_mobile_purchase`, which Meta counts as revenue for ads.
 *
 * The app's Meta factory builds one from its own event and sends it through
 * [PurchaseEventTracker][io.github.mkhytarmkhoian.herald.meta.trackers.PurchaseEventTracker].
 */
public data class MetaPurchaseEvent(
    /** Usually the app event's own. It isn't sent: Meta receives `fb_mobile_purchase`. */
    override val name: String,

    /** The amount paid. */
    public val amount: Double,

    /** An ISO 4217 code, such as `EUR`. */
    public val currency: String,

    /** Sent with the purchase. Usually the app event's own. */
    override val parameters: Map<String, AnalyticsValue> = emptyMap(),
) : Event {
    init {
        require(amount.isFinite()) { "Meta purchase '$name' needs a finite amount, not $amount" }
    }
}
