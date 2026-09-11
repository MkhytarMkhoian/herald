package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Event
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import io.github.mkhytarmkhoian.herald.Resolution

class CompositeFirebaseEventTrackerFactoryTest {

    private val event: Event = mockk()
    private val tracker: FirebaseEventTracker = mockk()
    private val laterTracker: FirebaseEventTracker = mockk()

    private val declines = FirebaseEventTrackerFactory { Resolution.Declined }
    private val claims = FirebaseEventTrackerFactory { Resolution.Claimed(tracker) }
    private val claimsLater = FirebaseEventTrackerFactory { Resolution.Claimed(laterTracker) }
    private val claimsAndDrops = FirebaseEventTrackerFactory { Resolution.Dropped }

    @Test
    fun `On create should skip declining factories and take the first claim`() {
        val factory = CompositeFirebaseEventTrackerFactory(declines, declines, claims, claimsLater)

        val trackers = factory.create(event).handlers

        assertEquals(1, trackers.size)
        assertSame(tracker, trackers.first())
    }

    @Test
    fun `On create an earlier factory should win, which is how a consumer overrides a marker`() {
        val factory = CompositeFirebaseEventTrackerFactory(claimsLater, claims)

        assertSame(laterTracker, factory.create(event).handlers.first())
    }

    @Test
    fun `On create Dropped should claim the event and stop the chain`() {
        val factory = CompositeFirebaseEventTrackerFactory(claimsAndDrops, claims)

        val resolution = factory.create(event)

        assertEquals(Resolution.Dropped, resolution)
    }

    @Test
    fun `On create should decline when every factory declines`() {
        val factory = CompositeFirebaseEventTrackerFactory(declines, declines)

        assertEquals(Resolution.Declined, factory.create(event))
    }

    @Test
    fun `On create should decline when the chain is empty`() {
        val factory = CompositeFirebaseEventTrackerFactory(emptyList())

        assertEquals(Resolution.Declined, factory.create(event))
    }
}
