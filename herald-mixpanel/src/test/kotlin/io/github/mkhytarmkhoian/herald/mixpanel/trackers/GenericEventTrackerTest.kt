package io.github.mkhytarmkhoian.herald.mixpanel.trackers

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class GenericEventTrackerTest {

    companion object {
        private const val EVENT_NAME = "Test event name"
        private const val CUSTOM_PARAM = "custom_param"
        private const val CUSTOM_PARAM_VALUE = "custom_param_value"
    }

    private val parameters = mapOf<String, AnalyticsValue>(
        CUSTOM_PARAM to AnalyticsValue.String(CUSTOM_PARAM_VALUE),
        "seats" to AnalyticsValue.Int(3),
        "trial" to AnalyticsValue.Boolean(false),
    )
    private val event: Event = mockk {
        every { name } returns EVENT_NAME
        every { parameters } returns this@GenericEventTrackerTest.parameters
    }
    private val mixpanel: MixpanelAPI = mockk(relaxed = true)

    private val genericEventTracker = GenericEventTracker(event, mixpanel)

    @Test
    fun `On track should track the event name and its parameters on mixpanel`() = runTest {
        genericEventTracker.track()

        verify {
            mixpanel.trackMap(
                EVENT_NAME,
                mapOf(CUSTOM_PARAM to CUSTOM_PARAM_VALUE, "seats" to 3, "trial" to false),
            )
        }
    }
}
