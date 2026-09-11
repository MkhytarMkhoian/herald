package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Identity
import io.github.mkhytarmkhoian.herald.parameters
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FakeAnalyticsProviderTest {

    private val analytics = FakeAnalyticsProvider()

    @Test
    fun `Every capability lands in one timeline, in order`() = runTest {
        analytics.start()
        analytics.identify(Identity("user-1"))
        analytics.set(property("plan", AnalyticsValue.String("pro")))
        analytics.track(event("checkout_started"))
        analytics.setEnabled(false)
        analytics.flush()
        analytics.reset()

        assertEquals(
            listOf(
                AnalyticsRecord.Started,
                AnalyticsRecord.Identified(Identity("user-1")),
                AnalyticsRecord.PropertySet(analytics.properties.single()),
                AnalyticsRecord.Tracked(analytics.events.single()),
                AnalyticsRecord.EnabledSet(false),
                AnalyticsRecord.Flushed,
                AnalyticsRecord.Reset,
            ),
            analytics.records,
        )
    }

    @Test
    fun `A property set before an event is visible as such, which two lambdas could not show`() =
        runTest {
            analytics.set(property("plan", AnalyticsValue.String("pro")))
            analytics.track(event("checkout_started"))

            val kinds = analytics.records.map { it::class.simpleName }

            assertEquals(listOf("PropertySet", "Tracked"), kinds)
        }

    @Test
    fun `clear forgets the records and the assertions already made`() = runTest {
        analytics.track(event("checkout_started"))
        analytics.assertTracked("checkout_started")

        analytics.clear()

        analytics.assertNothingTracked()
        analytics.assertNothingElseTracked()
    }

    @Test
    fun `assertTracked checks parameters by type, not by their printed form`() = runTest {
        analytics.track(
            event(
                "checkout_started",
                parameters {
                    put("plan", "pro")
                    put("seats", 3)
                    put("trial", false)
                },
            )
        )

        analytics.assertTracked("checkout_started") {
            param("plan", "pro")
            param("seats", 3)
            param("trial", false)
        }
    }

    @Test
    fun `Every param overload wraps its own AnalyticsValue type`() = runTest {
        analytics.track(
            event(
                "typed",
                parameters {
                    put("text", "3")
                    put("int", 3)
                    put("long", 3L)
                    put("float", 3f)
                    put("double", 3.0)
                    put("flag", true)
                },
            )
        )

        analytics.assertTracked("typed") {
            param("text", "3")
            param("int", 3)
            param("long", 3L)
            param("float", 3f)
            param("double", 3.0)
            param("flag", true)
        }
    }

    @Test
    fun `An overload that wrapped the wrong type would be caught, so each is distinct`() = runTest {
        analytics.track(event("typed", parameters { put("count", 3L) }))

        // Same printed form, different AnalyticsValue: only the Long overload may match.
        analytics.assertTracked("typed") { param("count", 3L) }
        assertFailsWith<AssertionError> {
            analytics.assertTracked("typed") { param("count", 3) }
        }
        assertFailsWith<AssertionError> {
            analytics.assertTracked("typed") { param("count", "3") }
        }
        assertFailsWith<AssertionError> {
            analytics.assertTracked("typed") { param("count", 3.0) }
        }
    }

    @Test
    fun `assertNotTracked passes when the event is absent`() = runTest {
        analytics.track(event("cart_viewed"))

        analytics.assertNotTracked("checkout_started")
    }

    @Test
    fun `assertPropertySet and assertIdentified pass on a match`() = runTest {
        analytics.set(property("total_purchases", AnalyticsValue.Int(42)))
        analytics.identify(Identity("user-1"))

        analytics.assertPropertySet("total_purchases", AnalyticsValue.Int(42))
        analytics.assertIdentified("user-1")
    }

    @Test
    fun `assertPropertySet checks the current value, so an overwrite is what counts`() = runTest {
        analytics.set(property("plan", AnalyticsValue.String("free")))
        analytics.set(property("plan", AnalyticsValue.String("pro")))

        analytics.assertPropertySet("plan", "pro")
    }

    @Test
    fun `Every assertPropertySet overload wraps its own AnalyticsValue type`() = runTest {
        analytics.set(property("text", AnalyticsValue.String("3")))
        analytics.set(property("int", AnalyticsValue.Int(3)))
        analytics.set(property("long", AnalyticsValue.Long(3L)))
        analytics.set(property("float", AnalyticsValue.Float(3f)))
        analytics.set(property("double", AnalyticsValue.Double(3.0)))
        analytics.set(property("flag", AnalyticsValue.Boolean(true)))

        analytics.assertPropertySet("text", "3")
        analytics.assertPropertySet("int", 3)
        analytics.assertPropertySet("long", 3L)
        analytics.assertPropertySet("float", 3f)
        analytics.assertPropertySet("double", 3.0)
        analytics.assertPropertySet("flag", true)
        assertFailsWith<AssertionError> { analytics.assertPropertySet("int", "3") }
        assertFailsWith<AssertionError> { analytics.assertPropertySet("int", 3L) }
    }

    @Test
    fun `assertIdentified is historical, so a later reset does not undo it`() = runTest {
        analytics.identify(Identity("user-1"))
        analytics.reset()

        analytics.assertIdentified("user-1")
    }

    @Test
    fun `noParameters passes on an event that carries none`() = runTest {
        analytics.track(event("checkout_started"))

        analytics.assertTracked("checkout_started") { noParameters() }
    }

    @Test
    fun `param takes an AnalyticsValue directly, for a value built elsewhere`() = runTest {
        analytics.track(event("checkout_started", parameters { put("seats", 3) }))

        analytics.assertTracked("checkout_started") { param("seats", AnalyticsValue.Int(3)) }
    }

    @Test
    fun `assertTrackedTimes accepts zero for an event that must not appear`() = runTest {
        analytics.track(event("cart_viewed"))

        analytics.assertTrackedTimes("checkout_started", 0)
    }

    @Test
    fun `The event assertions ignore records that are not events`() = runTest {
        analytics.start()
        analytics.set(property("plan", AnalyticsValue.String("pro")))
        analytics.identify(Identity("user-1"))

        analytics.assertNothingTracked()
        analytics.assertNothingElseTracked()
    }

    @Test
    fun `Records survive being written from many coroutines at once`() {
        val fake = FakeAnalyticsProvider()
        val threads = 8
        val perThread = 500

        val workers = List(threads) { worker ->
            Thread {
                repeat(perThread) { index ->
                    kotlinx.coroutines.runBlocking { fake.track(event("event_${worker}_$index")) }
                }
            }
        }
        workers.forEach(Thread::start)
        workers.forEach(Thread::join)

        assertEquals(threads * perThread, fake.events.size)
        assertEquals(threads * perThread, fake.events.map { it.name }.toSet().size)
    }

    @Test
    fun `A snapshot of records is not affected by later calls`() = runTest {
        analytics.track(event("first"))
        val snapshot = analytics.records

        analytics.track(event("second"))

        assertEquals(1, snapshot.size)
        assertTrue(analytics.records.size == 2)
    }
}
