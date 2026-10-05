package samples.vendors

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.mixpanel.android.mpmetrics.MixpanelOptions
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.mixpanel.CompositeMixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.CompositeMixpanelPropertySetterFactory
import io.github.mkhytarmkhoian.herald.mixpanel.GenericMixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.GenericMixpanelPropertySetterFactory
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelAnalyticsService
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.mixpanel.ScreenViewMixpanelEventTrackerFactory
import io.github.mkhytarmkhoian.herald.mixpanel.UserPropertyMixpanelPropertySetterFactory

fun mixpanelProvider(context: Context, projectToken: String): Herald.Provider {
    // --8<-- [start:provider]
    val mixpanel = MixpanelAPI.getInstance(
        context,
        projectToken,
        false, // trackAutomaticEvents
        MixpanelOptions.Builder().optOutTrackingDefault(true).build(), // silent until consent
    )

    val tracker = MixpanelAnalyticsTrackerService(
        CompositeMixpanelEventTrackerFactory(
            ScreenViewMixpanelEventTrackerFactory(mixpanel),
            GenericMixpanelEventTrackerFactory(mixpanel),
        ),
        CompositeMixpanelPropertySetterFactory(
            UserPropertyMixpanelPropertySetterFactory(mixpanel), // people profile: must come first
            GenericMixpanelPropertySetterFactory(mixpanel),      // super properties
        ),
    )
    val service = MixpanelAnalyticsService(mixpanel)

    val provider = Herald.Provider(
        name = "mixpanel",
        events = tracker,
        properties = tracker,
        identity = service,
        lifecycle = service,
        consent = service,
    )
    // --8<-- [end:provider]
    return provider
}
