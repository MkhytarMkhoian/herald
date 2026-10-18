package io.github.mkhytarmkhoian.herald.meta

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class MetaAnalyticsTrackerServiceTest {

    private object SignUpFinished : Event {
        override val name = "sign_up_finished"
    }

    private val sent = mutableListOf<String>()

    @Test
    fun `On track should run every handler the factory claims, in order`() = runTest {
        val tracker = MetaAnalyticsTrackerService { event ->
            Resolution.Claimed(
                MetaEventTracker { sent += "${event.name} 1" },
                MetaEventTracker { sent += "${event.name} 2" },
            )
        }

        tracker.track(SignUpFinished)

        assertEquals(listOf("sign_up_finished 1", "sign_up_finished 2"), sent)
    }

    @Test
    fun `On track of a declined event should send nothing`() = runTest {
        val tracker = MetaAnalyticsTrackerService { Resolution.Declined }

        tracker.track(SignUpFinished)

        assertEquals(emptyList(), sent)
    }
}
