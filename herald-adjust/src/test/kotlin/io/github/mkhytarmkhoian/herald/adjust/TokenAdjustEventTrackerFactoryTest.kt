package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.trackers.TokenEventTracker
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class TokenAdjustEventTrackerFactoryTest {

    private open class SignUpCompleted : Event {
        override val name = "sign_up_completed"
    }

    private class SignUpCompletedWithGoogle : SignUpCompleted()

    private class SearchTapped : Event {
        override val name = "search_tapped"
    }

    private val sent = slot<AdjustEvent>()
    private val adjust: AdjustInstance = mockk { every { trackEvent(capture(sent)) } just runs }

    private val eventTrackerFactory =
        TokenAdjustEventTrackerFactory(listOf(AdjustToken<SignUpCompleted>("abc123")), adjust)

    @Test
    fun `On create should send an event of a listed class under its token`() = runTest {
        val eventTrackers = eventTrackerFactory.create(SignUpCompleted()).handlers

        assertEquals(1, eventTrackers.size)
        assertIs<TokenEventTracker>(eventTrackers.first()).track()
        assertEquals("abc123", sent.captured.eventToken)
    }

    @Test
    fun `On create should match by class, not by name`() {
        val renamed = object : SignUpCompleted() {
            override val name = "registration_finished"
        }

        assertIs<Resolution.Claimed<AdjustEventTracker>>(eventTrackerFactory.create(renamed))
    }

    @Test
    fun `On create should send a subclass of a listed class under its token`() {
        assertIs<Resolution.Claimed<AdjustEventTracker>>(
            eventTrackerFactory.create(SignUpCompletedWithGoogle()),
        )
    }

    @Test
    fun `On create should give each listed type its own token`() = runTest {
        val factory = TokenAdjustEventTrackerFactory(
            listOf(AdjustToken<SignUpCompleted>("abc123"), AdjustToken<SearchTapped>("def456")),
            adjust,
        )

        factory.create(SearchTapped()).handlers.single().track()

        assertEquals("def456", sent.captured.eventToken)
    }

    @Test
    fun `Creating it with a type listed twice should fail`() {
        assertFailsWith<IllegalArgumentException> {
            TokenAdjustEventTrackerFactory(
                listOf(AdjustToken<SignUpCompleted>("abc123"), AdjustToken<SignUpCompleted>("def456")),
                adjust,
            )
        }
    }

    @Test
    fun `On create should decline an event whose class has no token, which is most of them`() {
        assertEquals(Resolution.Declined, eventTrackerFactory.create(SearchTapped()))
    }
}
