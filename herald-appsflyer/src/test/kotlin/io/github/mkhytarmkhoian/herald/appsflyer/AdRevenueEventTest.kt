package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.MediationNetwork
import org.junit.Test
import kotlin.test.assertEquals

internal class AdRevenueEventTest {

    @Test
    fun `On toAFAdRevenueData should carry every field`() {
        val event = object : AdRevenueEvent {
            override val name = "ad_impression"
            override val monetizationNetwork = "unity"
            override val mediationNetwork = MediationNetwork.APPLOVIN_MAX
            override val revenue = 0.0042
            override val currency = "USD"
        }

        assertEquals(
            AFAdRevenueData("unity", MediationNetwork.APPLOVIN_MAX, "USD", 0.0042),
            event.toAFAdRevenueData(),
        )
    }
}
