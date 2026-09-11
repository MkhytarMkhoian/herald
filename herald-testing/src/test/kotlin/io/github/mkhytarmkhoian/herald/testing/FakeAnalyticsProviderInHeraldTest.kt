package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.parameters
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The fake registered as a real provider, so the test runs through an actual `Herald`.
 */
class FakeAnalyticsProviderInHeraldTest {

    private val analytics = FakeAnalyticsProvider()

    @Test
    fun `An event tracked through Herald reaches the fake`() = runTest {
        val herald = Herald {
            provider(name = "test", events = analytics, properties = analytics)
        }

        herald.track(event("checkout_started", parameters { put("plan", "pro") }))

        analytics.assertTracked("checkout_started") { param("plan", "pro") }
        analytics.assertNothingElseTracked()
    }

    @Test
    fun `A decorated provider is visible in what the fake did not receive`() = runTest {
        val herald = Herald {
            provider(
                name = "test",
                events = EventTrackerService { if (it.name != "debug_ping") analytics.track(it) },
            )
        }

        herald.track(event("checkout_started"))
        herald.track(event("debug_ping"))

        analytics.assertTracked("checkout_started")
        analytics.assertNotTracked("debug_ping")
        analytics.assertNothingElseTracked()
    }
}
