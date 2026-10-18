package io.github.mkhytarmkhoian.herald.meta.trackers

import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.meta.MetaEventTracker
import io.github.mkhytarmkhoian.herald.meta.MetaPurchaseEvent
import io.github.mkhytarmkhoian.herald.meta.toMetaParameters
import java.math.BigDecimal
import java.util.Currency

/** Logs one [MetaPurchaseEvent] as Meta's `fb_mobile_purchase`. */
public class PurchaseEventTracker(
    private val event: MetaPurchaseEvent,
    private val logger: AppEventsLogger,
) : MetaEventTracker {

    override suspend fun track() {
        logger.logPurchase(
            BigDecimal.valueOf(event.amount),
            Currency.getInstance(event.currency),
            event.parameters.toMetaParameters(),
        )
    }
}
