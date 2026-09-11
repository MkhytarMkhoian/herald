package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.adjust.trackers.TokenEventTracker
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import io.github.mkhytarmkhoian.herald.Resolution

internal class TokenAdjustEventTrackerFactoryTest {

    companion object {
        private const val TOKENED_EVENT = "registration_completed"
        private const val EVENT_TOKEN = "abc123"
    }

    private val adjust: AdjustInstance = mockk()

    private val eventTrackerFactory =
        TokenAdjustEventTrackerFactory(mapOf(TOKENED_EVENT to EVENT_TOKEN), adjust)

    @Test
    fun `On create should return a TokenEventTracker for an event with a token`() {
        val event: Event = mockk { every { name } returns TOKENED_EVENT }

        val eventTrackers = eventTrackerFactory.create(event).handlers

        assertEquals(1, eventTrackers.size)
        assertIs<TokenEventTracker>(eventTrackers.first())
    }

    @Test
    fun `On create should decline an event with no token, which is most of them`() {
        val event: Event = mockk { every { name } returns "some_untokened_event" }

        assertEquals(Resolution.Declined, eventTrackerFactory.create(event))
    }
}
