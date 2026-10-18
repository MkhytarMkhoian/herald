package io.github.mkhytarmkhoian.herald.meta

import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.meta.trackers.GenericEventTracker
import io.github.mkhytarmkhoian.herald.meta.trackers.PurchaseEventTracker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class MetaTrackersTest {

    private object SearchTapped : Event {
        override val name = "search_tapped"
    }

    private val logger: AppEventsLogger = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putDouble(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun `toMetaParameters should keep numbers as numbers and write true or false as text`() {
        mapOf(
            "seats" to AnalyticsValue.Int(3),
            "watched_ms" to AnalyticsValue.Long(4L),
            "ratio" to AnalyticsValue.Float(0.1f),
            "price" to AnalyticsValue.Double(9.99),
            "plan" to AnalyticsValue.String("pro"),
            "trial" to AnalyticsValue.Boolean(false),
        ).toMetaParameters()

        verify { anyConstructed<Bundle>().putLong("seats", 3L) }
        verify { anyConstructed<Bundle>().putLong("watched_ms", 4L) }
        verify { anyConstructed<Bundle>().putDouble("ratio", 0.1) }
        verify { anyConstructed<Bundle>().putDouble("price", 9.99) }
        verify { anyConstructed<Bundle>().putString("plan", "pro") }
        verify { anyConstructed<Bundle>().putString("trial", "false") }
    }

    @Test
    fun `GenericEventTracker should log the event under its own name`() = runTest {
        GenericEventTracker(SearchTapped, logger).track()

        verify { logger.logEvent("search_tapped", any<Bundle>()) }
    }

    @Test
    fun `PurchaseEventTracker should log the purchase with its amount and currency`() = runTest {
        PurchaseEventTracker(MetaPurchaseEvent("ticket_bought", 4.99, "EUR"), logger).track()

        verify { logger.logPurchase(BigDecimal.valueOf(4.99), Currency.getInstance("EUR"), any<Bundle>()) }
    }

    @Test
    fun `A purchase with an amount that is not a finite number should be refused`() {
        assertFailsWith<IllegalArgumentException> { MetaPurchaseEvent("ticket_bought", Double.NaN, "EUR") }
    }

    @Test
    fun `The generic factory should claim every event, and the composite should use the first answer`() {
        val claimed = CompositeMetaEventTrackerFactory(
            MetaEventTrackerFactory { Resolution.Declined },
            GenericMetaEventTrackerFactory(logger),
        ).create(SearchTapped)

        assertIs<GenericEventTracker>(assertIs<Resolution.Claimed<MetaEventTracker>>(claimed).handlers.single())
    }

    @Test
    fun `A fallback factory that is not last should be refused`() {
        assertFailsWith<IllegalArgumentException> {
            CompositeMetaEventTrackerFactory(
                GenericMetaEventTrackerFactory(logger),
                MetaEventTrackerFactory { Resolution.Declined },
            )
        }
    }

    @Test
    fun `The require-mapped factory should fail for any event`() {
        val error = assertFailsWith<UnhandledEventException> {
            RequireMappedMetaEventTrackerFactory.create(SearchTapped)
        }
        assertEquals(SearchTapped, error.event)
    }
}
