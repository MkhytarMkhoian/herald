package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.AdRevenueEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.GenericEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.PurchaseEventTracker
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.SubscribeEventTracker
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class AppsFlyerFactoriesTest {

    private val appsFlyer: AppsFlyerLib = mockk()
    private val context: Context = mockk()

    @Test
    fun `Generic factory should claim any event, ad revenue included`() {
        val factory = GenericAppsFlyerEventTrackerFactory(appsFlyer, context)

        assertIs<GenericEventTracker>(factory.create(mockk<Event>()).handlers.single())
        assertIs<GenericEventTracker>(factory.create(mockk<AdRevenueEvent>()).handlers.single())
    }

    @Test
    fun `Ad revenue factory should claim an AdRevenueEvent and decline the rest`() {
        val factory = AdRevenueAppsFlyerEventTrackerFactory(appsFlyer)

        assertIs<AdRevenueEventTracker>(factory.create(mockk<AdRevenueEvent>()).handlers.single())
        assertEquals(Resolution.Declined, factory.create(mockk<Event>()))
    }

    @Test
    fun `Purchase factory should claim a PurchaseEvent and decline the rest`() {
        val factory = PurchaseAppsFlyerEventTrackerFactory(appsFlyer, context)

        assertIs<PurchaseEventTracker>(factory.create(mockk<PurchaseEvent>()).handlers.single())
        assertEquals(Resolution.Declined, factory.create(mockk<Event>()))
    }

    @Test
    fun `Subscribe factory should claim a SubscribeEvent and decline the rest`() {
        val factory = SubscribeAppsFlyerEventTrackerFactory(appsFlyer, context)

        assertIs<SubscribeEventTracker>(factory.create(mockk<SubscribeEvent>()).handlers.single())
        assertEquals(Resolution.Declined, factory.create(mockk<Event>()))
    }

    @Test
    fun `RequireMapped factory should throw, naming the event`() {
        val event: Event = mockk { every { name } returns "unmapped" }

        val failure = assertFailsWith<UnhandledEventException> {
            RequireMappedAppsFlyerEventTrackerFactory.create(event)
        }

        assertSame(event, failure.event)
    }
}
