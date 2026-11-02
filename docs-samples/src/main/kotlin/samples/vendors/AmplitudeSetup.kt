package samples.vendors

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeAnalyticsService
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeAnalyticsTrackerService
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTrackerFactory
import io.github.mkhytarmkhoian.herald.amplitude.CompositeAmplitudeEventTrackerFactory
import io.github.mkhytarmkhoian.herald.amplitude.CompositeAmplitudePropertySetterFactory
import io.github.mkhytarmkhoian.herald.amplitude.GenericAmplitudeEventTrackerFactory
import io.github.mkhytarmkhoian.herald.amplitude.GenericAmplitudePropertySetterFactory
import io.github.mkhytarmkhoian.herald.amplitude.ScreenViewAmplitudeEventTrackerFactory

fun amplitudeProvider(
    context: Context,
    apiKey: String,
    featureFactories: List<AmplitudeEventTrackerFactory>,
): Herald.Provider {
    // --8<-- [start:provider]
    // optOut = true: silent until consent. Autocapture is sessions only, so screen views come
    // from Herald's factory and are not counted twice.
    val amplitude = Amplitude(
        Configuration(
            apiKey,
            context,
            optOut = true,
            autocapture = setOf(AutocaptureOption.SESSIONS),
        ),
    )

    val tracker = AmplitudeAnalyticsTrackerService(
        CompositeAmplitudeEventTrackerFactory(
            featureFactories +                                     // your revenue trackers, among others
                ScreenViewAmplitudeEventTrackerFactory(amplitude) + // [Amplitude] Screen Viewed
                GenericAmplitudeEventTrackerFactory(amplitude),     // everything else
        ),
        CompositeAmplitudePropertySetterFactory(
            GenericAmplitudePropertySetterFactory(amplitude),  // user properties
        ),
    )
    val service = AmplitudeAnalyticsService(amplitude)

    val provider = Herald.Provider(
        name = "amplitude",
        events = tracker,
        properties = tracker,
        identity = service,
        lifecycle = service,
        consent = service,
    )
    // --8<-- [end:provider]
    return provider
}
