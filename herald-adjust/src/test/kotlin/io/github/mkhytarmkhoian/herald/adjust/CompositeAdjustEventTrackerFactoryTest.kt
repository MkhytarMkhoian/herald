package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class CompositeAdjustEventTrackerFactoryTest {

    private val event: Event = mockk()
    private val tracker: AdjustEventTracker = mockk()

    private val declining = AdjustEventTrackerFactory { Resolution.Declined }
    private val claiming = AdjustEventTrackerFactory { Resolution.Claimed(tracker) }
    private val dropping = AdjustEventTrackerFactory { Resolution.Dropped }

    @Test
    fun `On create should take the first answer that is not a decline`() {
        val composite = CompositeAdjustEventTrackerFactory(declining, claiming, dropping)

        val resolution = composite.create(event)

        assertSame(tracker, resolution.handlers.single())
    }

    @Test
    fun `On create a drop should stop the chain like a claim`() {
        val composite = CompositeAdjustEventTrackerFactory(dropping, claiming)

        assertEquals(Resolution.Dropped, composite.create(event))
    }

    @Test
    fun `On create should decline when every factory declines`() {
        val composite = CompositeAdjustEventTrackerFactory(declining, declining)

        assertEquals(Resolution.Declined, composite.create(event))
    }
}
