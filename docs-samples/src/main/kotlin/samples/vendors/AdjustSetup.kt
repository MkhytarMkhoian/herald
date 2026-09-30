package samples.vendors

import android.app.Application
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.adjust.AdjustAnalyticsService
import io.github.mkhytarmkhoian.herald.adjust.AdjustAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.adjust.AdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.adjust.CompositeAdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.adjust.CompositeAdjustPropertySetterFactory
import io.github.mkhytarmkhoian.herald.adjust.GenericAdjustPropertySetterFactory
import io.github.mkhytarmkhoian.herald.adjust.TokenAdjustEventTrackerFactory

fun adjustProvider(
    application: Application,
    appToken: String,
    featureFactories: List<AdjustEventTrackerFactory>,
): Herald.Provider {
    // --8<-- [start:provider]
    val adjust = Adjust.getDefaultInstance()
    val config = AdjustConfig(application, appToken, AdjustConfig.ENVIRONMENT_PRODUCTION).apply {
        setUrlStrategy(listOf("eu.adjust.com"), true, true) // EU data residency: your setting
        setLogLevel(LogLevel.SUPPRESS)
    }

    val tracker = AdjustAnalyticsTrackerService(
        CompositeAdjustEventTrackerFactory(
            featureFactories + // your revenue and ad-revenue trackers, among others
                TokenAdjustEventTrackerFactory(
                    tokens = mapOf("checkout_completed" to "abc123", "sign_up_finished" to "def456"),
                    adjust = adjust,
                ),
        ),
        CompositeAdjustPropertySetterFactory(
            GenericAdjustPropertySetterFactory(adjust),
        ),
    )
    val service = AdjustAnalyticsService(adjust, config) // start() calls initSdk(config)

    val provider = Herald.Provider(
        name = "adjust",
        events = tracker,
        properties = tracker,
        identity = service,
        lifecycle = service,
        consent = service,
    )
    // --8<-- [end:provider]
    return provider
}
