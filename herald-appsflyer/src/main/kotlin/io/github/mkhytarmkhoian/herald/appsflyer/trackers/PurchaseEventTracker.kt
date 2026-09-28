package io.github.mkhytarmkhoian.herald.appsflyer.trackers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFInAppEventType
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.PurchaseEvent
import io.github.mkhytarmkhoian.herald.appsflyer.toAppsFlyerEventValues

/** Logs one [PurchaseEvent] as AppsFlyer's `af_purchase`. */
public class PurchaseEventTracker(
    private val event: PurchaseEvent,
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTracker {

    override suspend fun track() {
        appsFlyer.logEvent(context, AFInAppEventType.PURCHASE, event.toAppsFlyerEventValues())
    }
}
