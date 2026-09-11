package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import io.github.mkhytarmkhoian.herald.Resolution

class RequireMappedFirebaseFactoriesTest {

    private val event: Event = mockk { every { name } returns "unwired_event" }
    private val property: Property = mockk { every { name } returns "unwired_property" }

    @Test
    fun `On create should throw and name the event nobody claimed`() {
        val failure = assertFailsWith<UnhandledEventException> {
            RequireMappedFirebaseEventTrackerFactory.create(event)
        }

        assertSame(event, failure.event)
    }

    @Test
    fun `On create should throw for an unclaimed property`() {
        val failure = assertFailsWith<UnhandledPropertyException> {
            RequireMappedFirebasePropertySetterFactory.create(property)
        }

        assertSame(property, failure.property)
    }

    @Test
    fun `At the end of a chain it should let claimed events through and fail only the rest`() {
        val tracker: FirebaseEventTracker = mockk()
        val claimed: Event = mockk { every { name } returns "wired_event" }
        val factory = CompositeFirebaseEventTrackerFactory(
            FirebaseEventTrackerFactory {
                if (it === claimed) Resolution.Claimed(tracker) else Resolution.Declined
            },
            RequireMappedFirebaseEventTrackerFactory,
        )

        assertEquals(Resolution.Claimed(tracker), factory.create(claimed))
        assertFailsWith<UnhandledEventException> { factory.create(event) }
    }
}
