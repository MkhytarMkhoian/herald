package io.github.mkhytarmkhoian.herald.adjust.trackers

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.adjust.AdjustRevenueEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class RevenueEventTrackerTest {

    private val adjust: AdjustInstance = mockk(relaxed = true)

    @Test
    fun `On track should send the tokened event with the revenue attached`() = runTest {
        val event = AdjustRevenueEvent(
            name = "purchase_completed",
            revenue = 9.99,
            currency = "USD",
            deduplicationId = "order-42",
            parameters = mapOf("plan" to AnalyticsValue.String("pro")),
        )

        RevenueEventTracker(event, "abc123", adjust).track()

        verify {
            adjust.trackEvent(withArg {
                assertEquals("abc123", it.eventToken)
                assertEquals(9.99, it.revenue)
                assertEquals("USD", it.currency)
                assertEquals("order-42", it.deduplicationId)
                assertEquals(mapOf("plan" to "pro"), it.callbackParameters)
            })
        }
    }
}
