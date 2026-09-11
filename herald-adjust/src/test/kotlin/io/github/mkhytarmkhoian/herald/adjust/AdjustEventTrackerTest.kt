package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AdjustEventTrackerTest {

    @Test
    fun `On toAdjustEvent should carry the token and the parameters as callback parameters`() {
        val eventToken = "Test event token"
        val eventParameters = mapOf(
            "Test param 1" to AnalyticsValue.Int(100),
            "Test param 2" to AnalyticsValue.String("Test string value"),
            "Test param 3" to AnalyticsValue.Boolean(true),
        )
        val event: Event = mockk {
            every { parameters } returns eventParameters
        }

        val result = event.toAdjustEvent(eventToken)

        assertEquals(eventToken, result.eventToken)
        // Adjust callback parameters are strings only, so a whole number flattens without a
        // decimal point rather than reading as "100.0".
        assertEquals(
            mapOf(
                "Test param 1" to "100",
                "Test param 2" to "Test string value",
                "Test param 3" to "true",
            ),
            result.callbackParameters,
        )
    }

    @Test
    fun `On toAdjustEvent a RevenueEvent should carry its revenue and currency`() {
        val event = object : RevenueEvent {
            override val name = "purchase_completed"
            override val revenue = 9.99
            override val currency = "USD"
            override val parameters = mapOf<String, AnalyticsValue>("plan" to AnalyticsValue.String("pro"))
        }

        val result = event.toAdjustEvent("abc123")

        assertEquals("abc123", result.eventToken)
        assertEquals(9.99, result.revenue)
        assertEquals("USD", result.currency)
        assertEquals(mapOf("plan" to "pro"), result.callbackParameters)
    }

    @Test
    fun `On toAdjustEvent a RevenueEvent with a deduplication id should carry it`() {
        val event = object : RevenueEvent {
            override val name = "purchase_completed"
            override val revenue = 9.99
            override val currency = "USD"
            override val deduplicationId = "GPA.1234-5678-9012-34567"
        }

        val result = event.toAdjustEvent("abc123")

        assertEquals("GPA.1234-5678-9012-34567", result.deduplicationId)
    }

    @Test
    fun `On toAdjustEvent a RevenueEvent without one should leave it unset`() {
        val event = object : RevenueEvent {
            override val name = "purchase_completed"
            override val revenue = 9.99
            override val currency = "USD"
        }

        assertNull(event.toAdjustEvent("abc123").deduplicationId)
    }

    @Test
    fun `On toAdjustEvent a plain event should carry no revenue`() {
        val event: Event = mockk { every { parameters } returns emptyMap() }

        val result = event.toAdjustEvent("abc123")

        assertNull(result.revenue)
        assertNull(result.currency)
    }
}
