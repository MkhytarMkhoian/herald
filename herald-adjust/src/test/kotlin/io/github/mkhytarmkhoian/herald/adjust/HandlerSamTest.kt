package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * The two handler interfaces stay single-abstract-method, so a one-off handler in a custom factory
 * can be a lambda.
 */
class HandlerSamTest {

    private val calls = mutableListOf<String>()

    private val event: Event = object : Event {
        override val name = "checkout_started"
    }
    private val property: Property = object : Property {
        override val name = "plan"
        override val value = AnalyticsValue.String("pro")
    }

    @Test
    fun `A lambda satisfies both handler interfaces`() = runTest {
        val service = AdjustAnalyticsTrackerService(
            eventTrackerFactory = CompositeAdjustEventTrackerFactory(
                AdjustEventTrackerFactory { Resolution.Claimed(AdjustEventTracker { calls += "track:${it.name}" }) },
            ),
            propertySetterFactory = CompositeAdjustPropertySetterFactory(
                AdjustPropertySetterFactory { Resolution.Claimed(AdjustPropertySetter { calls += "set:${it.name}" }) },
            ),
        )

        service.track(event)
        service.set(property)

        assertEquals(listOf("track:checkout_started", "set:plan"), calls)
    }
}
