package io.github.mkhytarmkhoian.herald.amplitude

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RevenueEventTest {

    private class Purchase(
        override val quantity: Int = 1,
        override val productId: String? = null,
        override val revenueType: String? = null,
        override val currency: String? = null,
        override val revenue: Double? = null,
        override val receipt: String? = null,
        override val receiptSig: String? = null,
    ) : RevenueEvent {
        override val name = "purchase"
        override val price = 4.99
        override val parameters = mapOf<String, AnalyticsValue>(
            "plan" to AnalyticsValue.String("pro"),
            "seats" to AnalyticsValue.Int(3),
        )
    }

    @Test
    fun `On toAmplitudeRevenue should carry every field, parameters as typed properties`() {
        val result = Purchase(
            quantity = 2,
            productId = "pro_monthly",
            revenueType = "subscription",
            currency = "EUR",
            revenue = 8.99,
            receipt = "receipt",
            receiptSig = "signature",
        ).toAmplitudeRevenue()

        assertEquals<Double?>(4.99, result.price)
        assertEquals(2, result.quantity)
        assertEquals("pro_monthly", result.productId)
        assertEquals("subscription", result.revenueType)
        assertEquals("EUR", result.currency)
        assertEquals<Double?>(8.99, result.revenue)
        assertEquals("receipt", result.receipt)
        assertEquals("signature", result.receiptSig)
        assertEquals<Map<String, Any?>?>(mapOf("plan" to "pro", "seats" to 3), result.properties)
    }

    @Test
    fun `On toAmplitudeRevenue an absent optional should stay unset, not become a default`() {
        val result = Purchase().toAmplitudeRevenue()

        assertEquals(1, result.quantity)
        assertNull(result.productId)
        assertNull(result.revenueType)
        assertNull(result.currency)
        assertNull(result.revenue)
        assertNull(result.receipt)
        assertNull(result.receiptSig)
    }
}
