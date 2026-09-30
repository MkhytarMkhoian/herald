package samples

import android.app.Application
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.log.AnalyticsLogger
import io.github.mkhytarmkhoian.herald.log.CompositeLogEventTrackerFactory
import io.github.mkhytarmkhoian.herald.log.CompositeLogPropertySetterFactory
import io.github.mkhytarmkhoian.herald.log.GenericLogEventTrackerFactory
import io.github.mkhytarmkhoian.herald.log.GenericLogPropertySetterFactory
import io.github.mkhytarmkhoian.herald.log.LogAnalyticsService
import io.github.mkhytarmkhoian.herald.log.LogAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.log.ScreenViewLogEventTrackerFactory
import io.github.mkhytarmkhoian.herald.parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// --8<-- [start:event]
data class CheckoutStarted(val plan: String, val seats: Int) : Event {
    override val name = "checkout_started"
    override val parameters = parameters {
        put("plan", plan)
        put("seats", seats)
    }
}
// --8<-- [end:event]

fun createHerald(): Herald {
    // --8<-- [start:herald]
    // Where the log lines go.
    val logger = AnalyticsLogger { Log.d("analytics", it) }

    // Handles events and user properties, using Herald's ready-made factories.
    val logTracker = LogAnalyticsTrackerService(
        eventTrackerFactory = CompositeLogEventTrackerFactory(
            ScreenViewLogEventTrackerFactory(logger), // screen views
            GenericLogEventTrackerFactory(logger),    // every other event
        ),
        propertySetterFactory = CompositeLogPropertySetterFactory(
            GenericLogPropertySetterFactory(logger),
        ),
    )
    // Handles the rest: start-up, sign-in and sign-out, consent.
    val logService = LogAnalyticsService(logger)

    val herald = Herald {
        provider(
            name = "log",
            events = logTracker,
            properties = logTracker,
            identity = logService,
            lifecycle = logService,
            consent = logService,
        )
    }
    // --8<-- [end:herald]
    return herald
}

fun quickStartWithFirebase(firebaseAnalytics: FirebaseAnalytics): Herald {
    val logger = AnalyticsLogger { Log.d("analytics", it) }
    val logTracker = LogAnalyticsTrackerService(
        CompositeLogEventTrackerFactory(GenericLogEventTrackerFactory(logger)),
        CompositeLogPropertySetterFactory(GenericLogPropertySetterFactory(logger)),
    )
    val logService = LogAnalyticsService(logger)
    // --8<-- [start:add-firebase]
    val herald = Herald {
        provider(
            name = "log",
            events = logTracker,
            properties = logTracker,
            identity = logService,
            lifecycle = logService,
            consent = logService,
        )
        providers(listOf(firebaseProvider(firebaseAnalytics))) // the new line
    }
    // --8<-- [end:add-firebase]
    return herald
}

// --8<-- [start:track]
class CheckoutViewModel(private val analytics: EventTrackerService) {
    suspend fun onCheckout(plan: String, seats: Int) {
        analytics.track(CheckoutStarted(plan, seats))
    }
}
// --8<-- [end:track]

// --8<-- [start:app]
class App : Application() {
    // One scope for work that lives as long as the app.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Herald, injected by your DI setup as the interface for starting analytics.
    lateinit var analytics: AnalyticsLifecycleService

    override fun onCreate() {
        super.onCreate()
        appScope.launch { analytics.start() }
    }
}
// --8<-- [end:app]

// --8<-- [start:app-viewmodel]
class MainViewModel(private val analytics: AnalyticsLifecycleService) : ViewModel() {
    init {
        viewModelScope.launch { analytics.start() }
    }
}
// --8<-- [end:app-viewmodel]
