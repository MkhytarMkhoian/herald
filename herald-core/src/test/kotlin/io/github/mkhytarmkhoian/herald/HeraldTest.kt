package io.github.mkhytarmkhoian.herald

import io.mockk.coEvery
import io.mockk.every
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class HeraldTest {

    private val event: Event = mockk { every { name } returns "an_event" }
    private val property: Property = mockk { every { name } returns "a_property" }
    private val identity = Identity(userId = "user-1")

    private val firebaseEvents: EventTrackerService = mockk(relaxed = true)
    private val firebaseProperties: PropertyTrackerService = mockk(relaxed = true)
    private val firebaseIdentity: IdentifiableUserService = mockk(relaxed = true)
    private val firebaseLifecycle: AnalyticsLifecycleService = mockk(relaxed = true)
    private val firebaseConsent: ConsentService = mockk(relaxed = true)
    private val adjustEvents: EventTrackerService = mockk(relaxed = true)

    private val failures = mutableListOf<Triple<String, AnalyticsOperation, Throwable>>()
    private val reporter = AnalyticsErrorReporter { provider, operation, failure ->
        failures += Triple(provider, operation, failure)
    }

    private fun herald(block: HeraldBuilder.() -> Unit = {}) = Herald {
        provider(
            name = "firebase",
            events = firebaseEvents,
            properties = firebaseProperties,
            identity = firebaseIdentity,
            lifecycle = firebaseLifecycle,
            consent = firebaseConsent,
        )
        provider(name = "adjust", events = adjustEvents)
        errorReporter(reporter)
        block()
    }

    @Test
    fun `On track should reach every provider that tracks events`() = runTest {
        herald().track(event)

        coVerify { firebaseEvents.track(event) }
        coVerify { adjustEvents.track(event) }
    }

    @Test
    fun `On set should skip a provider that has no property capability`() = runTest {
        herald().set(property)

        coVerify { firebaseProperties.set(property) }
    }

    @Test
    fun `On identify and reset should reach every provider that identifies`() = runTest {
        val herald = herald()

        herald.identify(identity)
        herald.reset()

        coVerify { firebaseIdentity.identify(identity) }
        coVerify { firebaseIdentity.reset() }
    }

    @Test
    fun `On lifecycle calls should reach every provider that has one`() = runTest {
        val herald = herald()

        herald.start()
        herald.flush()

        coVerify { firebaseLifecycle.start() }
        coVerify { firebaseLifecycle.flush() }
    }

    @Test
    fun `On setEnabled should reach the consent service, not the lifecycle`() = runTest {
        val herald = herald()

        herald.setEnabled(true)

        coVerify { firebaseConsent.setEnabled(true) }
    }

    @Test
    fun `A provider registered with consent alone is accepted`() = runTest {
        val herald = Herald { provider(name = "firebase", consent = firebaseConsent) }

        herald.setEnabled(false)

        coVerify { firebaseConsent.setEnabled(false) }
    }

    // Herald has no filtering of its own: wrap the provider instead.
    @Test
    fun `On track a decorated provider should keep the event from that provider alone`() = runTest {
        val herald = Herald {
            provider(name = "firebase", events = firebaseEvents)
            provider(
                name = "adjust",
                events = EventTrackerService { if (it.name != "an_event") adjustEvents.track(it) },
            )
            errorReporter(reporter)
        }

        herald.track(event)

        coVerify { firebaseEvents.track(event) }
        coVerify(exactly = 0) { adjustEvents.track(event) }
    }

    @Test
    fun `On track a failing provider should not stop the others and should be reported`() = runTest {
        val failure = IllegalStateException("firebase is down")
        coEvery { firebaseEvents.track(event) } throws failure

        herald().track(event)

        coVerify { adjustEvents.track(event) }
        assertEquals(1, failures.size)
        val (provider, operation, reported) = failures.single()
        assertEquals("firebase", provider)
        assertEquals(AnalyticsOperation.Track("an_event"), operation)
        assertSame(failure, reported)
    }

    // A provider that only completes once the other has been called: this deadlocks under a
    // sequential fan-out.
    @Test
    // runBlocking, not runTest: the fan-out uses Dispatchers.Default, and runTest's withTimeout
    // measures *virtual* time, which elapses instantly against work on real threads.
    fun `On track providers should run concurrently rather than one after another`() = runBlocking {
        val adjustCalled = CompletableDeferred<Unit>()
        val slowEvents: EventTrackerService = mockk()
        coEvery { slowEvents.track(event) } coAnswers { adjustCalled.await() }
        coEvery { adjustEvents.track(event) } coAnswers { adjustCalled.complete(Unit) }

        val herald = Herald {
            provider(name = "slow", events = slowEvents)
            provider(name = "adjust", events = adjustEvents)
            errorReporter(reporter)
        }

        withTimeout(5_000) { herald.track(event) }

        coVerify { adjustEvents.track(event) }
        assertContentEquals(emptyList(), failures)
    }

    // The reported operation has to name the failing event.
    @Test
    fun `A reported failure should name the event that caused it`() = runTest {
        val other: Event = mockk { every { name } returns "other_event" }
        coEvery { firebaseEvents.track(other) } throws IllegalStateException("bad name")

        herald().track(event)
        herald().track(other)

        assertEquals(listOf(AnalyticsOperation.Track("other_event")), failures.map { it.second })
    }

    @Test
    fun `A reported property failure should name the property, and carry no value`() = runTest {
        coEvery { firebaseProperties.set(property) } throws IllegalStateException("nope")

        herald().set(property)

        assertEquals(AnalyticsOperation.SetProperty("a_property"), failures.single().second)
    }

    @Test
    fun `A throwing error reporter should not escape to the caller`() = runTest {
        coEvery { firebaseEvents.track(event) } throws IllegalStateException("firebase is down")

        val herald = Herald {
            provider(name = "firebase", events = firebaseEvents)
            provider(name = "adjust", events = adjustEvents)
            errorReporter { _, _, _ -> throw IllegalStateException("the crash reporter is down") }
        }

        herald.track(event)

        coVerify { adjustEvents.track(event) }
    }

    @Test
    fun `A reporter that throws for one provider should still be given the others`() = runTest {
        coEvery { firebaseEvents.track(event) } throws IllegalStateException("firebase is down")
        coEvery { adjustEvents.track(event) } throws IllegalStateException("adjust is down")
        val seen = mutableListOf<String>()

        val herald = Herald {
            provider(name = "firebase", events = firebaseEvents)
            provider(name = "adjust", events = adjustEvents)
            errorReporter { provider, _, _ ->
                seen += provider
                if (provider == "firebase") throw IllegalStateException("the crash reporter is down")
            }
        }

        herald.track(event)

        assertContentEquals(listOf("firebase", "adjust"), seen)
    }

    @Test
    fun `On track cancellation should propagate rather than be contained`() = runTest {
        coEvery { firebaseEvents.track(event) } throws CancellationException("scope closed")

        assertFailsWith<CancellationException> { herald().track(event) }

        // Deliberately no assertion about the peer provider: the fan-out is concurrent, so adjust
        // is already in flight by the time firebase cancels. What must hold is that cancellation
        // reaches the caller and is never reported as a vendor failure.
        assertContentEquals(emptyList(), failures)
    }

    @Test
    fun `On track should run on the configured dispatcher`() = runTest {
        val dispatcher = RecordingDispatcher()

        herald { dispatcher(dispatcher) }.track(event)

        assertEquals(true, dispatcher.dispatched)
    }

    @Test
    fun `A provider with no capabilities should be rejected at build time`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            Herald { provider(name = "empty") }
        }

        assertEquals(true, failure.message?.contains("no capabilities"))
    }

    @Test
    fun `A provider with a blank name should be rejected at build time`() {
        assertFailsWith<IllegalArgumentException> {
            Herald { provider(name = " ", events = firebaseEvents) }
        }
    }

    @Test
    fun `A Provider built directly should be validated the same way`() {
        assertFailsWith<IllegalArgumentException> { Herald.Provider(name = "empty") }
        assertFailsWith<IllegalArgumentException> { Herald.Provider(name = " ", events = firebaseEvents) }
    }

    @Test
    fun `On providers should register a collection in iteration order alongside provider`() = runTest {
        val seen = mutableListOf<String>()
        val first: EventTrackerService = mockk { coEvery { track(event) } answers { seen += "first" } }
        val second: EventTrackerService = mockk { coEvery { track(event) } answers { seen += "second" } }
        coEvery { firebaseEvents.track(event) } answers { seen += "firebase" }

        val herald = Herald {
            providers(
                listOf(
                    Herald.Provider(name = "first", events = first),
                    Herald.Provider(name = "second", events = second),
                )
            )
            provider(name = "firebase", events = firebaseEvents)
            // Unconfined runs each async body inline, so the call order is the registration order.
            dispatcher(Dispatchers.Unconfined)
        }

        herald.track(event)

        assertContentEquals(listOf("first", "second", "firebase"), seen)
    }
}
