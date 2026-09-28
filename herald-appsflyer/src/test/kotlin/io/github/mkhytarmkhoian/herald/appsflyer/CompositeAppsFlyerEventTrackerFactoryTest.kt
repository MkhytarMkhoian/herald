package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class CompositeAppsFlyerEventTrackerFactoryTest {

    private val event: Event = mockk()
    private val tracker: AppsFlyerEventTracker = mockk()

    private val declining = AppsFlyerEventTrackerFactory { Resolution.Declined }
    private val claiming = AppsFlyerEventTrackerFactory { Resolution.Claimed(tracker) }
    private val dropping = AppsFlyerEventTrackerFactory { Resolution.Dropped }

    @Test
    fun `On create should take the first answer that is not a decline`() {
        val composite = CompositeAppsFlyerEventTrackerFactory(declining, claiming, dropping)

        assertSame(tracker, composite.create(event).handlers.single())
    }

    @Test
    fun `On create a drop should stop the chain like a claim`() {
        val composite = CompositeAppsFlyerEventTrackerFactory(dropping, claiming)

        assertEquals(Resolution.Dropped, composite.create(event))
    }

    @Test
    fun `On create should decline when every factory declines`() {
        val composite = CompositeAppsFlyerEventTrackerFactory(declining, declining)

        assertEquals(Resolution.Declined, composite.create(event))
    }

    @Test
    fun `A catch-all anywhere but last should fail when the chain is built`() {
        val generic = GenericAppsFlyerEventTrackerFactory(mockk<AppsFlyerLib>(), mockk<Context>())

        assertFailsWith<IllegalArgumentException> {
            CompositeAppsFlyerEventTrackerFactory(generic, claiming)
        }
    }
}
