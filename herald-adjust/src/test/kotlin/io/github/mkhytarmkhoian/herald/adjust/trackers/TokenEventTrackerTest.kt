package io.github.mkhytarmkhoian.herald.adjust.trackers

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class TokenEventTrackerTest {

    companion object {
        private const val EVENT_TOKEN = "Test event token"
        private const val CUSTOM_PARAM = "custom_param"
        private const val CUSTOM_PARAM_VALUE = "custom_param_value"
    }

    private val event: Event = mockk {
        every { parameters } returns mapOf(CUSTOM_PARAM to AnalyticsValue.String(CUSTOM_PARAM_VALUE))
    }
    private val adjust: AdjustInstance = mockk(relaxed = true)

    private val tokenEventTracker = TokenEventTracker(event, EVENT_TOKEN, adjust)

    @Test
    fun `On track should send the event under its token`() = runTest {
        tokenEventTracker.track()

        verify {
            adjust.trackEvent(withArg {
                assertEquals(EVENT_TOKEN, it.eventToken)
                assertEquals(mapOf(CUSTOM_PARAM to CUSTOM_PARAM_VALUE), it.callbackParameters)
            })
        }
    }
}
