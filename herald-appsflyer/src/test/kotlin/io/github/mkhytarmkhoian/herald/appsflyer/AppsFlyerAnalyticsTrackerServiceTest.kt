package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AppsFlyerAnalyticsTrackerServiceTest {

    private val eventTrackerFactory: AppsFlyerEventTrackerFactory = mockk()

    private val service = AppsFlyerAnalyticsTrackerService(eventTrackerFactory)

    @Test
    fun `On track should run every handler the chain returned`() = runTest {
        val first: AppsFlyerEventTracker = mockk(relaxed = true)
        val second: AppsFlyerEventTracker = mockk(relaxed = true)
        every { eventTrackerFactory.create(any()) } returns Resolution.Claimed(first, second)

        service.track(mockk<Event>())

        coVerify { first.track() }
        coVerify { second.track() }
    }
}
