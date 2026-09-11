package io.github.mkhytarmkhoian.herald.log.trackers

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.log.RecordingLogger
import io.github.mkhytarmkhoian.herald.parameters
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class LogTrackerTest {

    private val logger = RecordingLogger()

    @Test
    fun `An event prints as one log call, so overlapping events cannot interleave`() = runTest {
        val event = object : Event {
            override val name = "checkout_started"
            override val parameters = parameters {
                put("plan", "pro")
                put("seats", 3)
            }
        }

        GenericEventTracker(event, logger).track()

        assertEquals(
            listOf(
                """
                [herald] event   checkout_started
                    ├─ plan  = pro
                    └─ seats = 3
                """.trimIndent()
            ),
            logger.messages,
        )
    }

    @Test
    fun `A screen view prints the screen name, and its parameters too`() = runTest {
        val event = object : ScreenViewEvent {
            override val name = "checkout_opened"
            override val screenName = "CheckoutScreen"
            override val parameters = parameters { put("source", "cart") }
        }

        ScreenViewEventTracker(event, logger).track()

        assertEquals(
            listOf(
                """
                [herald] screen  CheckoutScreen
                    └─ source = cart
                """.trimIndent()
            ),
            logger.messages,
        )
    }
}
