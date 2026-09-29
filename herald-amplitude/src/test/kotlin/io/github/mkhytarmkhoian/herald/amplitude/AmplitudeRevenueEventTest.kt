package io.github.mkhytarmkhoian.herald.amplitude

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AmplitudeRevenueEventTest {

    private fun purchase(
        quantity: Int = 1,
        productId: String? = null,
        revenueType: String? = null,
        currency: String? = null,
        revenue: Double? = null,
        receipt: String? = null,
        receiptSig: String? = null,
    ) = AmplitudeRevenueEvent(
        name = "purchase",
        price = 4.99,
        quantity = quantity,
        productId = productId,
        revenueType = revenueType,
        currency = currency,
        revenue = revenue,
        receipt = receipt,
        receiptSig = receiptSig,
        parameters = mapOf(
            "plan" to AnalyticsValue.String("pro"),
            "seats" to AnalyticsValue.Int(3),
        ),
    )

    @Test
    fun `On toAmplitudeRevenue should carry every field, parameters as typed properties`() {
        val result = purchase(
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
        val result = purchase().toAmplitudeRevenue()

        assertEquals(1, result.quantity)
        assertNull(result.productId)
        assertNull(result.revenueType)
        assertNull(result.currency)
        assertNull(result.revenue)
        assertNull(result.receipt)
        assertNull(result.receiptSig)
    }
}
