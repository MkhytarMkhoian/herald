package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Identity
import io.github.mkhytarmkhoian.herald.parameters
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Every assertion has to fail when it should, and say why.
 */
class FakeAnalyticsProviderFailureTest {

    private val analytics = FakeAnalyticsProvider()

    private fun assertFails(block: () -> Unit): String =
        assertFailsWith<AssertionError>(block = block).message.orEmpty()

    @Test
    fun `assertTracked fails when the event was never tracked, and shows what was`() = runTest {
        analytics.track(event("cart_viewed"))

        val message = assertFails { analytics.assertTracked("checkout_started") }

        assertTrue("checkout_started" in message, message)
        assertTrue("cart_viewed" in message, message)
    }

    @Test
    fun `assertTracked fails on a duplicate, and points at assertTrackedTimes`() = runTest {
        analytics.track(event("checkout_started"))
        analytics.track(event("checkout_started"))

        val message = assertFails { analytics.assertTracked("checkout_started") }

        assertTrue("2 were tracked" in message, message)
        assertTrue("assertTrackedTimes" in message, message)
    }

    @Test
    fun `assertTracked fails on a missing parameter`() = runTest {
        analytics.track(event("checkout_started", parameters { put("plan", "pro") }))

        val message = assertFails {
            analytics.assertTracked("checkout_started") { param("seats", 3) }
        }

        assertTrue("seats" in message, message)
    }

    @Test
    fun `assertTracked fails when a number was sent as text`() = runTest {
        analytics.track(event("checkout_started", parameters { put("seats", "3") }))

        val message = assertFails {
            analytics.assertTracked("checkout_started") { param("seats", 3) }
        }

        assertTrue("Int(3)" in message, message)
        assertTrue("String(3)" in message, message)
    }

    @Test
    fun `noParameters fails when the event carries some`() = runTest {
        analytics.track(event("checkout_started", parameters { put("plan", "pro") }))

        val message = assertFails { analytics.assertTracked("checkout_started") { noParameters() } }

        assertTrue("plan" in message, message)
    }

    @Test
    fun `assertNothingElseTracked fails on an event nobody expected`() = runTest {
        analytics.track(event("checkout_started"))
        analytics.track(event("debug_ping"))

        analytics.assertTracked("checkout_started")
        val message = assertFails { analytics.assertNothingElseTracked() }

        assertTrue("debug_ping" in message, message)
    }

    @Test
    fun `assertNothingElseTracked passes once every event has been named`() = runTest {
        analytics.track(event("checkout_started"))
        analytics.track(event("cart_viewed"))
        analytics.track(event("cart_viewed"))

        analytics.assertTracked("checkout_started")
        analytics.assertTrackedTimes("cart_viewed", 2)

        analytics.assertNothingElseTracked()
    }

    @Test
    fun `assertTrackedTimes fails on the wrong count`() = runTest {
        analytics.track(event("checkout_started"))

        val message = assertFails { analytics.assertTrackedTimes("checkout_started", 2) }

        assertTrue("2 times" in message, message)
        assertTrue("1 times" in message, message)
    }

    @Test
    fun `assertNotTracked and assertNothingTracked fail when something was tracked`() = runTest {
        analytics.track(event("checkout_started"))

        assertFails { analytics.assertNotTracked("checkout_started") }
        assertFails { analytics.assertNothingTracked() }
    }

    @Test
    fun `assertPropertySet fails on a different value, and names the one that was set`() = runTest {
        analytics.set(property("total_purchases", AnalyticsValue.Int(41)))

        val message = assertFails {
            analytics.assertPropertySet("total_purchases", AnalyticsValue.Int(42))
        }

        assertTrue("Int(41)" in message, message)
    }

    @Test
    fun `assertPropertySet fails when the right value was overwritten, and shows the sequence`() = runTest {
        analytics.set(property("plan", AnalyticsValue.String("pro")))
        analytics.set(property("plan", AnalyticsValue.String("free")))

        val message = assertFails { analytics.assertPropertySet("plan", "pro") }

        assertTrue("String(pro) then String(free)" in message, message)
    }

    @Test
    fun `assertPropertySet fails when a number was set as text`() = runTest {
        analytics.set(property("seats", AnalyticsValue.String("3")))

        val message = assertFails { analytics.assertPropertySet("seats", 3) }

        assertTrue("Int(3)" in message, message)
        assertTrue("String(3)" in message, message)
    }

    @Test
    fun `assertPropertySet fails when the property was never set at all`() = runTest {
        analytics.set(property("plan", AnalyticsValue.String("pro")))

        val message = assertFails {
            analytics.assertPropertySet("total_purchases", AnalyticsValue.Int(42))
        }

        assertTrue("never was" in message, message)
        assertTrue("plan" in message, message)
    }

    @Test
    fun `assertIdentified fails for a different user`() = runTest {
        analytics.identify(Identity("user-2"))

        val message = assertFails { analytics.assertIdentified("user-1") }

        assertTrue("user-1" in message, message)
        assertTrue("user-2" in message, message)
    }

    @Test
    fun `The timeline renders every kind of record, since any failure may have to show one`() =
        runTest {
            analytics.start()
            analytics.identify(Identity("user-1"))
            analytics.set(property("plan", AnalyticsValue.String("pro")))
            analytics.track(event("cart_viewed", parameters { put("items", 2) }))
            analytics.setEnabled(false)
            analytics.flush()
            analytics.reset()

            val message = assertFails { analytics.assertTracked("checkout_started") }

            assertEquals(
                listOf(
                    "  1. start",
                    "  2. identify user-1",
                    "  3. property plan = pro",
                    "  4. event    cart_viewed { items = 2 }",
                    "  5. enabled  false",
                    "  6. flush",
                    "  7. reset",
                ).joinToString(separator = "\n"),
                message.substringAfter("Recorded:\n"),
            )
        }

    @Test
    fun `A failure with nothing recorded says so instead of showing an empty list`() {
        val message = assertFails { analytics.assertTracked("checkout_started") }

        assertTrue("nothing was recorded" in message, message)
    }
}
