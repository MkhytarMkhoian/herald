package io.github.mkhytarmkhoian.herald.amplitude.trackers

import com.amplitude.android.Amplitude
import com.amplitude.core.events.EventOptions
import com.amplitude.core.events.Revenue
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeRevenueEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AmplitudeTrackersTest {

    private val amplitude: Amplitude = mockk(relaxed = true)

    private val parameters = mapOf<String, AnalyticsValue>(
        "plan" to AnalyticsValue.String("pro"),
        "seats" to AnalyticsValue.Int(3),
        "trial" to AnalyticsValue.Boolean(false),
    )

    @Test
    fun `Generic tracker should track the event name with typed properties`() = runTest {
        val event: Event = mockk {
            every { name } returns "checkout_started"
            every { parameters } returns this@AmplitudeTrackersTest.parameters
        }

        GenericEventTracker(event, amplitude).track()

        verify {
            amplitude.track("checkout_started", mapOf("plan" to "pro", "seats" to 3, "trial" to false))
        }
    }

    @Test
    fun `Screen view tracker should use Amplitude's reserved screen view names`() = runTest {
        val event: ScreenViewEvent = mockk {
            every { screenName } returns "Checkout"
            every { parameters } returns mapOf("plan" to AnalyticsValue.String("pro"))
        }

        ScreenViewEventTracker(event, amplitude).track()

        verify {
            amplitude.track(
                "[Amplitude] Screen Viewed",
                mapOf("plan" to "pro", "[Amplitude] Screen Name" to "Checkout"),
            )
        }
    }

    @Test
    fun `Revenue tracker should send through the revenue API, not as an event`() = runTest {
        val event = AmplitudeRevenueEvent(
            name = "purchase",
            price = 4.99,
            currency = "EUR",
        )

        RevenueEventTracker(event, amplitude).track()

        val revenue = slot<Revenue>()
        verify { amplitude.revenue(capture(revenue), isNull()) }
        verify(inverse = true) { amplitude.track(any<String>(), any()) }
        assertEquals<Double?>(4.99, revenue.captured.price)
        assertEquals("EUR", revenue.captured.currency)
    }

    @Test
    fun `Revenue tracker should pass the insert id as an option, so a retried purchase counts once`() = runTest {
        val event = AmplitudeRevenueEvent(
            name = "purchase",
            price = 4.99,
            insertId = "order-42",
        )

        RevenueEventTracker(event, amplitude).track()

        val options = mutableListOf<EventOptions?>()
        verify { amplitude.revenue(any<Revenue>(), captureNullable(options)) }
        assertEquals("order-42", options.single()?.insertId)
        assertNull(options.single()?.userId) // Only the insert id is overridden.
    }
}
