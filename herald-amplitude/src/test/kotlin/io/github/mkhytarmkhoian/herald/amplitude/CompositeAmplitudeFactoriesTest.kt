package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class CompositeAmplitudeFactoriesTest {

    private val event: Event = mockk()
    private val tracker: AmplitudeEventTracker = mockk()

    private val declining = AmplitudeEventTrackerFactory { Resolution.Declined }
    private val claiming = AmplitudeEventTrackerFactory { Resolution.Claimed(tracker) }
    private val dropping = AmplitudeEventTrackerFactory { Resolution.Dropped }

    @Test
    fun `On create should take the first answer that is not a decline`() {
        val composite = CompositeAmplitudeEventTrackerFactory(declining, claiming, dropping)

        assertSame(tracker, composite.create(event).handlers.single())
    }

    @Test
    fun `On create a drop should stop the chain like a claim`() {
        val composite = CompositeAmplitudeEventTrackerFactory(dropping, claiming)

        assertEquals(Resolution.Dropped, composite.create(event))
    }

    @Test
    fun `On create should decline when every factory declines`() {
        val composite = CompositeAmplitudeEventTrackerFactory(declining, declining)

        assertEquals(Resolution.Declined, composite.create(event))
    }

    @Test
    fun `A catch-all anywhere but last should fail when the chain is built`() {
        val amplitude: Amplitude = mockk()

        assertFailsWith<IllegalArgumentException> {
            CompositeAmplitudeEventTrackerFactory(GenericAmplitudeEventTrackerFactory(amplitude), claiming)
        }
        assertFailsWith<IllegalArgumentException> {
            CompositeAmplitudePropertySetterFactory(
                GenericAmplitudePropertySetterFactory(amplitude),
                AmplitudePropertySetterFactory { Resolution.Declined },
            )
        }
    }
}
