package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RevenueEventsTest {

    private fun purchase(
        contentId: String? = null,
        contentType: String? = null,
        quantity: Int? = null,
        orderId: String? = null,
        parameters: Map<String, AnalyticsValue> = emptyMap(),
    ) = AppsFlyerPurchaseEvent(
        name = "checkout_completed",
        revenue = 9.99,
        currency = "EUR",
        contentId = contentId,
        contentType = contentType,
        quantity = quantity,
        orderId = orderId,
        parameters = parameters,
    )

    @Test
    fun `On AppsFlyerPurchaseEvent toAppsFlyerEventValues should carry every field under its af key`() {
        val values = purchase(
            contentId = "pro_monthly",
            contentType = "subscription_plan",
            quantity = 2,
            orderId = "order-42",
            parameters = mapOf("plan" to AnalyticsValue.String("pro")),
        ).toAppsFlyerEventValues()

        assertEquals(
            mapOf<String, Any>(
                "plan" to "pro",
                "af_revenue" to 9.99,
                "af_currency" to "EUR",
                "af_content_id" to "pro_monthly",
                "af_content_type" to "subscription_plan",
                "af_quantity" to 2,
                "af_order_id" to "order-42",
            ),
            values,
        )
    }

    @Test
    fun `On AppsFlyerPurchaseEvent toAppsFlyerEventValues an absent optional should be left out, not sent empty`() {
        assertEquals(
            mapOf<String, Any>("af_revenue" to 9.99, "af_currency" to "EUR"),
            purchase().toAppsFlyerEventValues(),
        )
    }

    @Test
    fun `On AppsFlyerPurchaseEvent toAppsFlyerEventValues a parameter with a key it sets should be refused`() {
        val event = purchase(parameters = mapOf("af_revenue" to AnalyticsValue.Double(1.0)))

        assertFailsWith<IllegalArgumentException> { event.toAppsFlyerEventValues() }
    }

    @Test
    fun `On AppsFlyerPurchaseEvent toAppsFlyerEventValues a key it leaves empty should be free for a parameter`() {
        val values = purchase(parameters = mapOf("af_order_id" to AnalyticsValue.String("order-1")))
            .toAppsFlyerEventValues()

        assertEquals("order-1", values["af_order_id"])
    }

    @Test
    fun `On AppsFlyerSubscribeEvent toAppsFlyerEventValues a parameter with a key it sets should be refused`() {
        val event = AppsFlyerSubscribeEvent(
            name = "subscription_started",
            revenue = 4.99,
            currency = "USD",
            parameters = mapOf<String, AnalyticsValue>("af_currency" to AnalyticsValue.String("EUR")),
        )

        assertFailsWith<IllegalArgumentException> { event.toAppsFlyerEventValues() }
    }

    @Test
    fun `On AppsFlyerSubscribeEvent toAppsFlyerEventValues should carry revenue and currency with the parameters`() {
        val event = AppsFlyerSubscribeEvent(
            name = "subscription_started",
            revenue = 4.99,
            currency = "USD",
            parameters = mapOf<String, AnalyticsValue>("plan" to AnalyticsValue.String("pro")),
        )

        assertEquals(
            mapOf<String, Any>("plan" to "pro", "af_revenue" to 4.99, "af_currency" to "USD"),
            event.toAppsFlyerEventValues(),
        )
    }
}
