package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AdRevenueEventTest {

    private class Impression(
        override val adImpressionsCount: Int? = null,
        override val adRevenueNetwork: String? = null,
        override val adRevenueUnit: String? = null,
        override val adRevenuePlacement: String? = null,
    ) : AdRevenueEvent {
        override val name = "ad_impression"
        override val source = "applovin_max_sdk"
        override val revenue = 0.0042
        override val currency = "USD"
        override val parameters = mapOf<String, AnalyticsValue>("format" to AnalyticsValue.String("rewarded"))
    }

    @Test
    fun `On toAdjustAdRevenue should carry every field`() {
        val event = Impression(
            adImpressionsCount = 1,
            adRevenueNetwork = "Unity Ads",
            adRevenueUnit = "rewarded_main",
            adRevenuePlacement = "level_end",
        )

        val result = event.toAdjustAdRevenue()

        assertEquals("applovin_max_sdk", result.source)
        assertEquals(0.0042, result.revenue)
        assertEquals("USD", result.currency)
        assertEquals(1, result.adImpressionsCount)
        assertEquals("Unity Ads", result.adRevenueNetwork)
        assertEquals("rewarded_main", result.adRevenueUnit)
        assertEquals("level_end", result.adRevenuePlacement)
        assertEquals(mapOf("format" to "rewarded"), result.callbackParameters)
    }

    @Test
    fun `On toAdjustAdRevenue an absent optional should stay unset, not become a default`() {
        val result = Impression().toAdjustAdRevenue()

        assertNull(result.adImpressionsCount)
        assertNull(result.adRevenueNetwork)
        assertNull(result.adRevenueUnit)
        assertNull(result.adRevenuePlacement)
    }
}
