package io.github.mkhytarmkhoian.herald.mixpanel.trackers

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ScreenViewEventTrackerTest {

    companion object {
        private const val SCREEN = "Test screen"
        private const val CUSTOM_PARAM = "custom_param"
        private const val CUSTOM_PARAM_VALUE = "custom_param_value"
    }

    private val event: ScreenViewEvent = mockk {
        every { screenName } returns SCREEN
        every { parameters } returns mapOf(CUSTOM_PARAM to AnalyticsValue.String(CUSTOM_PARAM_VALUE))
    }

    private val properties = mapOf<String, Any>(
        CUSTOM_PARAM to CUSTOM_PARAM_VALUE,
        "screen_name" to SCREEN,
    )

    private val mixpanel: MixpanelAPI = mockk(relaxed = true)

    private val screenViewEventTracker = ScreenViewEventTracker(event, mixpanel)

    @Test
    fun `On track should track screen view event with the screen name added`() = runTest {
        screenViewEventTracker.track()

        verify { mixpanel.trackMap("screen_view", properties) }
    }
}
