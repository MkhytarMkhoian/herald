package io.github.mkhytarmkhoian.herald.appsflyer

import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.MediationNetwork
import org.junit.Test
import kotlin.test.assertEquals

internal class AppsFlyerAdRevenueEventTest {

    @Test
    fun `On toAFAdRevenueData should carry every field`() {
        val event = AppsFlyerAdRevenueEvent(
            name = "ad_impression",
            monetizationNetwork = "unity",
            mediationNetwork = MediationNetwork.APPLOVIN_MAX,
            revenue = 0.0042,
            currency = "USD",
        )

        assertEquals(
            AFAdRevenueData("unity", MediationNetwork.APPLOVIN_MAX, "USD", 0.0042),
            event.toAFAdRevenueData(),
        )
    }
}
