package io.github.mkhytarmkhoian.herald.appsflyer.trackers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.toAppsFlyerEventValues

public class GenericEventTracker(
    private val event: Event,
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTracker {

    override suspend fun track() {
        appsFlyer.logEvent(context, event.name, event.parameters.toAppsFlyerEventValues())
    }
}
