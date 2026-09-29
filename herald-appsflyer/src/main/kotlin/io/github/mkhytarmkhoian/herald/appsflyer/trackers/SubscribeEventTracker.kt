package io.github.mkhytarmkhoian.herald.appsflyer.trackers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFInAppEventType
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerSubscribeEvent
import io.github.mkhytarmkhoian.herald.appsflyer.toAppsFlyerEventValues

/** Logs one [AppsFlyerSubscribeEvent] as AppsFlyer's `af_subscribe`. */
public class SubscribeEventTracker(
    private val event: AppsFlyerSubscribeEvent,
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTracker {

    override suspend fun track() {
        appsFlyer.logEvent(context, AFInAppEventType.SUBSCRIBE, event.toAppsFlyerEventValues())
    }
}
