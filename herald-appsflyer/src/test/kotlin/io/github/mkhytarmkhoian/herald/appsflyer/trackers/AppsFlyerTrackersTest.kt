package io.github.mkhytarmkhoian.herald.appsflyer.trackers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.MediationNetwork
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.appsflyer.AdRevenueEvent
import io.github.mkhytarmkhoian.herald.appsflyer.PurchaseEvent
import io.github.mkhytarmkhoian.herald.appsflyer.SubscribeEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AppsFlyerTrackersTest {

    private val appsFlyer: AppsFlyerLib = mockk(relaxed = true)
    private val context: Context = mockk()

    @Test
    fun `Generic tracker should log the event name with typed values`() = runTest {
        val event: Event = mockk {
            every { name } returns "af_purchase"
            every { parameters } returns mapOf(
                "af_revenue" to AnalyticsValue.Double(9.99),
                "af_currency" to AnalyticsValue.String("EUR"),
            )
        }

        GenericEventTracker(event, appsFlyer, context).track()

        verify {
            appsFlyer.logEvent(context, "af_purchase", mapOf("af_revenue" to 9.99, "af_currency" to "EUR"))
        }
    }

    @Test
    fun `Purchase tracker should log af_purchase with the revenue under af_revenue`() = runTest {
        val event = object : PurchaseEvent {
            override val name = "checkout_completed"
            override val revenue = 9.99
            override val currency = "EUR"
        }

        PurchaseEventTracker(event, appsFlyer, context).track()

        verify { appsFlyer.logEvent(context, "af_purchase", mapOf("af_revenue" to 9.99, "af_currency" to "EUR")) }
        verify(inverse = true) { appsFlyer.logEvent(any(), "checkout_completed", any()) }
    }

    @Test
    fun `Subscribe tracker should log af_subscribe with the revenue under af_revenue`() = runTest {
        val event = object : SubscribeEvent {
            override val name = "subscription_started"
            override val revenue = 4.99
            override val currency = "USD"
        }

        SubscribeEventTracker(event, appsFlyer, context).track()

        verify { appsFlyer.logEvent(context, "af_subscribe", mapOf("af_revenue" to 4.99, "af_currency" to "USD")) }
    }

    @Test
    fun `Ad revenue tracker should send through the ad revenue API, parameters as additional ones`() = runTest {
        val event = object : AdRevenueEvent {
            override val name = "ad_impression"
            override val monetizationNetwork = "unity"
            override val mediationNetwork = MediationNetwork.GOOGLE_ADMOB
            override val revenue = 0.01
            override val currency = "USD"
            override val parameters = mapOf<String, AnalyticsValue>("format" to AnalyticsValue.String("rewarded"))
        }

        AdRevenueEventTracker(event, appsFlyer).track()

        verify {
            appsFlyer.logAdRevenue(
                AFAdRevenueData("unity", MediationNetwork.GOOGLE_ADMOB, "USD", 0.01),
                mapOf("format" to "rewarded"),
            )
        }
        verify(inverse = true) { appsFlyer.logEvent(any(), any(), any()) }
    }
}
