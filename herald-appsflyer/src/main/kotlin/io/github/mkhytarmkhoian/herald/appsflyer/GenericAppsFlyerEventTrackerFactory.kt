package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.GenericEventTracker

/**
 * Logs any event under its own name with its parameters as event values. Claims everything, so it
 * only belongs last in a chain — anything after it is unreachable.
 *
 * Most apps omit it and send AppsFlyer only their conversions, each claimed by a factory of its
 * own; then only the events some factory claims reach it.
 */
public class GenericAppsFlyerEventTrackerFactory(
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AppsFlyerEventTrackerFactory, FallbackFactory {

    override fun create(event: Event): Resolution<AppsFlyerEventTracker> =
        Resolution.Claimed(GenericEventTracker(event, appsFlyer, context))
}
