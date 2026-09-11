package io.github.mkhytarmkhoian.herald.adjust.trackers

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.adjust.AdRevenueEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class AdRevenueEventTrackerTest {

    private val event = object : AdRevenueEvent {
        override val name = "ad_impression"
        override val source = "admob_sdk"
        override val revenue = 0.01
        override val currency = "EUR"
    }
    private val adjust: AdjustInstance = mockk(relaxed = true)

    @Test
    fun `On track should send the event through the ad-revenue API, not trackEvent`() = runTest {
        AdRevenueEventTracker(event, adjust).track()

        verify {
            adjust.trackAdRevenue(withArg {
                assertEquals("admob_sdk", it.source)
                assertEquals(0.01, it.revenue)
                assertEquals("EUR", it.currency)
            })
        }
        verify(exactly = 0) { adjust.trackEvent(any()) }
    }
}
