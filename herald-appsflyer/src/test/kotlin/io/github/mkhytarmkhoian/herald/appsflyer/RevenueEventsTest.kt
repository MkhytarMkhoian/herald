package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals

internal class RevenueEventsTest {

    private class Purchase(
        override val contentId: String? = null,
        override val contentType: String? = null,
        override val quantity: Int? = null,
        override val orderId: String? = null,
        override val parameters: Map<String, AnalyticsValue> = emptyMap(),
    ) : PurchaseEvent {
        override val name = "checkout_completed"
        override val revenue = 9.99
        override val currency = "EUR"
    }

    @Test
    fun `On PurchaseEvent toAppsFlyerEventValues should carry every field under its af key`() {
        val values = Purchase(
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
    fun `On PurchaseEvent toAppsFlyerEventValues an absent optional should be left out, not sent empty`() {
        assertEquals(
            mapOf<String, Any>("af_revenue" to 9.99, "af_currency" to "EUR"),
            Purchase().toAppsFlyerEventValues(),
        )
    }

    @Test
    fun `On PurchaseEvent toAppsFlyerEventValues a typed field should win over a parameter of the same key`() {
        val values = Purchase(parameters = mapOf("af_revenue" to AnalyticsValue.Double(1.0))).toAppsFlyerEventValues()

        assertEquals(9.99, values["af_revenue"])
    }

    @Test
    fun `On SubscribeEvent toAppsFlyerEventValues should carry revenue and currency with the parameters`() {
        val event = object : SubscribeEvent {
            override val name = "subscription_started"
            override val revenue = 4.99
            override val currency = "USD"
            override val parameters = mapOf<String, AnalyticsValue>("plan" to AnalyticsValue.String("pro"))
        }

        assertEquals(
            mapOf<String, Any>("plan" to "pro", "af_revenue" to 4.99, "af_currency" to "USD"),
            event.toAppsFlyerEventValues(),
        )
    }
}
