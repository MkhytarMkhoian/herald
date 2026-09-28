package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * The handler interface stays single-abstract-method, so a one-off handler in a custom factory can
 * be a lambda.
 */
class HandlerSamTest {

    @Test
    fun `A lambda satisfies the handler interface`() = runTest {
        val calls = mutableListOf<String>()
        val service = AppsFlyerAnalyticsTrackerService(
            CompositeAppsFlyerEventTrackerFactory(
                AppsFlyerEventTrackerFactory { Resolution.Claimed(AppsFlyerEventTracker { calls += "track:${it.name}" }) },
            ),
        )

        service.track(object : Event {
            override val name = "checkout_started"
        })

        assertEquals(listOf("track:checkout_started"), calls)
    }
}
