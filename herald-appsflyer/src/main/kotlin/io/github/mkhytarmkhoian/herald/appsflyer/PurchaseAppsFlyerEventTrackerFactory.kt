package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.PurchaseEventTracker

/** Claims every [PurchaseEvent] and declines everything else. */
public class PurchaseAppsFlyerEventTrackerFactory(
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTrackerFactory {

    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
        is PurchaseEvent -> Resolution.Claimed(PurchaseEventTracker(event, appsFlyer, context))
        else -> Resolution.Declined
    }
}
