package samples

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustInstance
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.Herald
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import io.github.mkhytarmkhoian.herald.adjust.AdjustAnalyticsService
import io.github.mkhytarmkhoian.herald.adjust.AdjustAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.adjust.CompositeAdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.adjust.CompositeAdjustPropertySetterFactory
import io.github.mkhytarmkhoian.herald.adjust.GenericAdjustPropertySetterFactory
import io.github.mkhytarmkhoian.herald.adjust.TokenAdjustEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.CompositeFirebasePropertySetterFactory
import io.github.mkhytarmkhoian.herald.firebase.FirebaseAnalyticsService
import io.github.mkhytarmkhoian.herald.firebase.FirebaseAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.firebase.GenericFirebasePropertySetterFactory
import io.github.mkhytarmkhoian.herald.firebase.ScreenViewFirebaseEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.CompositeMixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.CompositeMixpanelPropertySetterFactory
import io.github.mkhytarmkhoian.herald.mixpanel.GenericMixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.GenericMixpanelPropertySetterFactory
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelAnalyticsService
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.mixpanel.ScreenViewMixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.UserPropertyMixpanelPropertySetterFactory
import samples.guides.AnalyticsConsentRepository

// --8<-- [start:firebase-provider]
fun firebaseProvider(firebaseAnalytics: FirebaseAnalytics): Herald.Provider {
    val tracker = FirebaseAnalyticsTrackerService(
        CompositeFirebaseEventTrackerFactory(
            ScreenViewFirebaseEventTrackerFactory(firebaseAnalytics), // GA4's reserved screen_view
            GenericFirebaseEventTrackerFactory(firebaseAnalytics),    // everything else, as-is
        ),
        CompositeFirebasePropertySetterFactory(
            GenericFirebasePropertySetterFactory(firebaseAnalytics),
        ),
    )
    val service = FirebaseAnalyticsService(firebaseAnalytics, identificationEnabled = true)
    return Herald.Provider(
        name = "firebase",
        events = tracker,
        properties = tracker,
        identity = service,
        lifecycle = service,
        consent = service,
    )
}
// --8<-- [end:firebase-provider]

fun mixpanelProvider(mixpanel: MixpanelAPI): Herald.Provider {
    val tracker = MixpanelAnalyticsTrackerService(
        CompositeMixpanelEventTrackerFactory(
            ScreenViewMixpanelEventTrackerFactory(mixpanel),
            GenericMixpanelEventTrackerFactory(mixpanel),
        ),
        CompositeMixpanelPropertySetterFactory(
            UserPropertyMixpanelPropertySetterFactory(mixpanel), // before the generic one
            GenericMixpanelPropertySetterFactory(mixpanel),
        ),
    )
    val service = MixpanelAnalyticsService(mixpanel, loggingEnabled = false, identificationEnabled = true)
    return Herald.Provider(
        name = "mixpanel",
        events = tracker,
        properties = tracker,
        identity = service,
        lifecycle = service,
        consent = service,
    )
}

fun adjustProvider(adjust: AdjustInstance, config: AdjustConfig, tokens: Map<String, String>): Herald.Provider {
    val tracker = AdjustAnalyticsTrackerService(
        CompositeAdjustEventTrackerFactory(
            TokenAdjustEventTrackerFactory(tokens, adjust), // only events with a dashboard token
        ),
        CompositeAdjustPropertySetterFactory(
            GenericAdjustPropertySetterFactory(adjust),
        ),
    )
    val service = AdjustAnalyticsService(adjust, config)
    return Herald.Provider(
        name = "adjust",
        events = tracker,
        properties = tracker,
        identity = service,
        lifecycle = service,
        consent = service,
    )
}

fun buildHerald(
    firebaseAnalytics: FirebaseAnalytics,
    mixpanel: MixpanelAPI,
    adjust: AdjustInstance,
    adjustConfig: AdjustConfig,
    adjustTokens: Map<String, String>,
): Herald {
    // --8<-- [start:herald]
    val herald = Herald {
        providers(
            listOf(
                firebaseProvider(firebaseAnalytics),
                mixpanelProvider(mixpanel),
                adjustProvider(adjust, adjustConfig, adjustTokens),
            ),
        )
        errorReporter { provider, operation, failure ->
            Log.w("analytics", "$provider failed on $operation", failure)
        }
    }
    // --8<-- [end:herald]
    return herald
}

fun heraldWithDispatcher(provider: Herald.Provider): Herald {
    // --8<-- [start:dispatcher]
    val herald = Herald {
        providers(listOf(provider))
        dispatcher(Dispatchers.IO)
    }
    // --8<-- [end:dispatcher]
    return herald
}

// --8<-- [start:start-from-application]
class MyApplication : Application() {
    // One scope for work that lives as long as the app.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Injected by your DI setup.
    lateinit var analytics: AnalyticsLifecycleService
    lateinit var consent: ConsentService
    lateinit var consentRepository: AnalyticsConsentRepository // however you store the answer

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            analytics.start()                                 // some vendors start quiet here
            consent.setEnabled(consentRepository.isEnabled()) // so re-apply the stored answer
        }
    }
}
// --8<-- [end:start-from-application]

// --8<-- [start:start-from-entry]
class MainActivityViewModel(
    private val analytics: AnalyticsLifecycleService,
    private val consent: ConsentService,
    private val consentRepository: AnalyticsConsentRepository,
) : ViewModel() {
    init {
        viewModelScope.launch {
            analytics.start()
            consent.setEnabled(consentRepository.isEnabled())
        }
    }
}
// --8<-- [end:start-from-entry]

