package io.github.mkhytarmkhoian.herald

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * The two tracker interfaces stay single-abstract-method, so a fake can be a lambda.
 */
class TrackerServiceSamTest {

    private val trackedEvents = mutableListOf<Event>()
    private val setProperties = mutableListOf<Property>()

    private val events = EventTrackerService { trackedEvents += it }
    private val properties = PropertyTrackerService { setProperties += it }

    @Test
    fun `A lambda satisfies both tracker interfaces`() = runTest {
        val event: Event = object : Event {
            override val name = "checkout_started"
        }
        val property: Property = object : Property {
            override val name = "plan"
            override val value = AnalyticsValue.String("pro")
        }

        val herald = Herald {
            provider(name = "recorder", events = events, properties = properties)
        }
        herald.track(event)
        herald.set(property)

        assertEquals(listOf(event), trackedEvents)
        assertEquals(listOf(property), setProperties)
    }
}
