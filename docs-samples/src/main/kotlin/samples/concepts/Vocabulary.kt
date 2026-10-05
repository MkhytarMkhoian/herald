package samples.concepts

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.UserProperty
import io.github.mkhytarmkhoian.herald.parameters

// --8<-- [start:no-parameters]
data object SignInTapped : Event {
    override val name = "sign_in_tapped"
}
// --8<-- [end:no-parameters]

// --8<-- [start:parameters]
data class PlanSelected(val plan: String, val seats: Int, val price: Double, val trial: Boolean) : Event {
    override val name = "plan_selected"
    override val parameters = parameters {
        put("plan", plan)     // AnalyticsValue.String
        put("seats", seats)   // AnalyticsValue.Int
        put("price", price)   // AnalyticsValue.Double
        put("trial", trial)   // AnalyticsValue.Boolean
    }
}
// --8<-- [end:parameters]

// --8<-- [start:screen-view]
data class ProductScreenViewed(val productId: String) : ScreenViewEvent {
    override val name = "product" // the screen's name in GA4, Mixpanel and Amplitude
    override val parameters = parameters { put("product_id", productId) }
}
// --8<-- [end:screen-view]

// --8<-- [start:properties]
// Describes the session: a super property in Mixpanel, sent with every later event.
data class AppTheme(val dark: Boolean) : Property {
    override val name = "app_theme"
    override val value = AnalyticsValue.String(if (dark) "dark" else "light")
}

// Describes the person: Mixpanel's people profile, Amplitude's user properties.
class PurchaseCount(count: Int) : UserProperty {
    override val name = "total_purchases"
    override val value = AnalyticsValue.Int(count)
}
// --8<-- [end:properties]
