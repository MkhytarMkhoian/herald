package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.trackers.AdRevenueEventTracker
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class AdRevenueAdjustEventTrackerFactoryTest {

    private val adjust: AdjustInstance = mockk()

    private val factory = AdRevenueAdjustEventTrackerFactory(adjust)

    @Test
    fun `On create should claim an AdRevenueEvent`() {
        val event = object : AdRevenueEvent {
            override val name = "ad_impression"
            override val source = "admob_sdk"
            override val revenue = 0.01
            override val currency = "USD"
        }

        val handlers = factory.create(event).handlers

        assertEquals(1, handlers.size)
        assertIs<AdRevenueEventTracker>(handlers.single())
    }

    @Test
    fun `On create should decline everything else, including a tokened purchase`() {
        val event: Event = mockk()

        assertEquals(Resolution.Declined, factory.create(event))
    }
}
