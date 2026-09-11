package io.github.mkhytarmkhoian.herald.firebase

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
        val service = FirebaseAnalyticsTrackerService(
            eventTrackerFactory = CompositeFirebaseEventTrackerFactory(
                FirebaseEventTrackerFactory { Resolution.Claimed(FirebaseEventTracker { calls += "track:${it.name}" }) },
            ),
            propertySetterFactory = CompositeFirebasePropertySetterFactory(
                FirebasePropertySetterFactory { Resolution.Claimed(FirebasePropertySetter { calls += "set:${it.name}" }) },
            ),
        )

        service.track(event)
        service.set(property)

        assertEquals(listOf("track:checkout_started", "set:plan"), calls)
    }
}
