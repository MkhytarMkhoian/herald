package samples.vendors

import android.app.Application
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerAnalyticsService
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.appsflyer.AppsFlyerEventTrackerFactory
import io.github.mkhytarmkhoian.herald.appsflyer.CompositeAppsFlyerEventTrackerFactory

// --8<-- [start:init]
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // AppsFlyer requires init in Application.onCreate, and version 7 ignores start()
        // until a session-ready listener is registered.
        AppsFlyerLib.getInstance()
            .init("YOUR_DEV_KEY", null, this)
            .registerSessionReadyListener { /* deep-link data is ready */ }
    }
}
// --8<-- [end:init]

fun appsFlyerProvider(
    application: Application,
    featureFactories: List<AppsFlyerEventTrackerFactory>,
): Herald.Provider {
    // --8<-- [start:provider]
    val appsFlyer = AppsFlyerLib.getInstance() // initialised in Application.onCreate

    val tracker = AppsFlyerAnalyticsTrackerService(
        // Your conversions and revenue. Nothing at the end, so other events aren't sent.
        CompositeAppsFlyerEventTrackerFactory(featureFactories),
    )
    val service = AppsFlyerAnalyticsService(appsFlyer, application)

    val provider = Herald.Provider(
        name = "appsflyer",
        events = tracker, // no properties: AppsFlyer keeps no user attributes
        identity = service,
        lifecycle = service,
        consent = service,
    )
    // --8<-- [end:provider]
    return provider
}
