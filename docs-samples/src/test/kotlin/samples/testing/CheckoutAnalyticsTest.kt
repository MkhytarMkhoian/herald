package samples.testing

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.PropertyTrackerService
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.testing.AnalyticsRecord
import io.github.mkhytarmkhoian.herald.testing.FakeAnalyticsProvider
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import samples.CheckoutStarted
import samples.CheckoutViewModel
import samples.concepts.CardNumberSeen
import samples.concepts.CheckoutCompleted
import samples.concepts.CheckoutFirebaseEventTrackerFactory
import samples.concepts.PlanSelected
import samples.concepts.PurchaseCount
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CheckoutAnalyticsTest {

    // --8<-- [start:fake-provider]
    @Test
    fun `checkout reports the plan and seats, once`() = runTest {
        val analytics = FakeAnalyticsProvider()
        val herald = Herald {
            provider(name = "test", events = analytics, properties = analytics)
            dispatcher(UnconfinedTestDispatcher(testScheduler))
        }

        CheckoutViewModel(analytics = herald).onCheckout(plan = "pro", seats = 3)

        analytics.assertTracked("checkout_started") {
            param("plan", "pro")
            param("seats", 3)      // an Int: the String "3" would fail here
        }
        analytics.assertNothingElseTracked()
    }
    // --8<-- [end:fake-provider]

    // --8<-- [start:order]
    @Test
    fun `the purchase count is set before the purchase event that should carry it`() = runTest {
        val analytics = FakeAnalyticsProvider()
        val properties: PropertyTrackerService = analytics
        val events: EventTrackerService = analytics

        properties.set(PurchaseCount(1))
        events.track(PlanSelected("pro", seats = 1, price = 9.99, trial = false))

        val kinds = analytics.records.map { it::class }
        assertEquals(listOf(AnalyticsRecord.PropertySet::class, AnalyticsRecord.Tracked::class), kinds)
    }
    // --8<-- [end:order]

    // --8<-- [start:lambda]
    @Test
    fun `a lambda is the smallest fake for one capability`() = runTest {
        val tracked = mutableListOf<Event>()

        CheckoutViewModel(analytics = EventTrackerService { tracked += it }).onCheckout("pro", 3)

        assertEquals(listOf<Event>(CheckoutStarted("pro", 3)), tracked)
    }
    // --8<-- [end:lambda]

    // --8<-- [start:factory]
    @Test
    fun `the checkout factory claims its events and declines the rest`() {
        val factory = CheckoutFirebaseEventTrackerFactory(mockk<FirebaseAnalytics>())

        assertIs<Resolution.Claimed<*>>(factory.create(CheckoutCompleted(9.99, "EUR")))
        assertEquals(Resolution.Dropped, factory.create(CardNumberSeen("4242")))
        assertEquals(Resolution.Declined, factory.create(CheckoutStarted("pro", 3)))
    }
    // --8<-- [end:factory]

    @Test
    fun `an assertion that cannot fail is not a test`() {
        val analytics = FakeAnalyticsProvider()
        assertTrue(runCatching { analytics.assertTracked("never") }.isFailure)
    }
}
