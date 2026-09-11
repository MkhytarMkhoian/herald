package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.mixpanel.trackers.ScreenViewEventTracker
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import io.github.mkhytarmkhoian.herald.Resolution

class ScreenViewMixpanelEventTrackerFactoryTest {

    private val mixpanel: MixpanelAPI = mockk()

    private val eventTrackerFactory = ScreenViewMixpanelEventTrackerFactory(mixpanel)

    @Test
    fun `On create with ScreenViewEvent should return ScreenViewEventTracker`() {
        val event: ScreenViewEvent = mockk()

        val eventTrackers = eventTrackerFactory.create(event).handlers

        assertEquals(1, eventTrackers.size)
        assertIs<ScreenViewEventTracker>(eventTrackers.first())
    }

    @Test
    fun `On create with an unmarked Event should decline it`() {
        val event: Event = mockk()

        assertEquals(Resolution.Declined, eventTrackerFactory.create(event))
    }
}
