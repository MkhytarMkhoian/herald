package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.SubscribeEventTracker

/** Claims every [SubscribeEvent] and declines everything else. */
public class SubscribeAppsFlyerEventTrackerFactory(
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTrackerFactory {

    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = when (event) {
        is SubscribeEvent -> Resolution.Claimed(SubscribeEventTracker(event, appsFlyer, context))
        else -> Resolution.Declined
    }
}
