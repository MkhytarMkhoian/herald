package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AdjustAdRevenueEventTest {

    private fun impression(
        adImpressionsCount: Int? = null,
        adRevenueNetwork: String? = null,
        adRevenueUnit: String? = null,
        adRevenuePlacement: String? = null,
    ) = AdjustAdRevenueEvent(
        name = "ad_impression",
        source = "applovin_max_sdk",
        revenue = 0.0042,
        currency = "USD",
        adImpressionsCount = adImpressionsCount,
        adRevenueNetwork = adRevenueNetwork,
        adRevenueUnit = adRevenueUnit,
        adRevenuePlacement = adRevenuePlacement,
        parameters = mapOf("format" to AnalyticsValue.String("rewarded")),
    )

    @Test
    fun `On toAdjustAdRevenue should carry every field`() {
        val event = impression(
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
        val result = impression().toAdjustAdRevenue()

        assertNull(result.adImpressionsCount)
        assertNull(result.adRevenueNetwork)
        assertNull(result.adRevenueUnit)
        assertNull(result.adRevenuePlacement)
    }
}
