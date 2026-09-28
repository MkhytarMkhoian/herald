package io.github.mkhytarmkhoian.herald.appsflyer.trackers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFInAppEventType
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.SubscribeEvent
import io.github.mkhytarmkhoian.herald.appsflyer.toAppsFlyerEventValues

/** Logs one [SubscribeEvent] as AppsFlyer's `af_subscribe`. */
public class SubscribeEventTracker(
    private val event: SubscribeEvent,
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTracker {

    override suspend fun track() {
        appsFlyer.logEvent(context, AFInAppEventType.SUBSCRIBE, event.toAppsFlyerEventValues())
    }
}
