package samples.concepts

import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService

// --8<-- [start:consumers]
class PaywallViewModel(private val analytics: EventTrackerService) {
    suspend fun onPlanSelected(plan: String, seats: Int) =
        analytics.track(PlanSelected(plan, seats, price = 9.99, trial = false))
}

class PrivacySettingsViewModel(private val consent: ConsentService) {
    suspend fun onAnalyticsToggled(allowed: Boolean) = consent.setEnabled(allowed)
}
// --8<-- [end:consumers]

fun lambdaFake() {
    // --8<-- [start:fake]
    val tracked = mutableListOf<Event>()
    val viewModel = PaywallViewModel(analytics = EventTrackerService { tracked += it })
    // --8<-- [end:fake]
    viewModel.hashCode()
}
