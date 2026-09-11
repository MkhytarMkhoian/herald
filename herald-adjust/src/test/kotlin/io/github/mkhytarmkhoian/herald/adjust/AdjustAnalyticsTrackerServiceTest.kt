package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import io.github.mkhytarmkhoian.herald.Resolution

internal class AdjustAnalyticsTrackerServiceTest {

    private val eventTrackerFactory: AdjustEventTrackerFactory = mockk()
    private val propertySetterFactory: AdjustPropertySetterFactory = mockk()

    private val adjustAnalyticsTrackerService =
        AdjustAnalyticsTrackerService(eventTrackerFactory, propertySetterFactory)

    @Test
    fun `On track should create correct event trackers and track them`() = runTest {
        val event: Event = mockk()
        val eventTracker1: AdjustEventTracker = mockk(relaxed = true)
        val eventTracker2: AdjustEventTracker = mockk(relaxed = true)
        every { eventTrackerFactory.create(any()) } returns Resolution.Claimed(eventTracker1, eventTracker2)

        adjustAnalyticsTrackerService.track(event)

        verify { eventTrackerFactory.create(event) }
        coVerify { eventTracker1.track() }
        coVerify { eventTracker2.track() }
    }

    @Test
    fun `On set should create correct property setters and set them`() = runTest {
        val property: Property = mockk()
        val propertySetter1: AdjustPropertySetter = mockk(relaxed = true)
        val propertySetter2: AdjustPropertySetter = mockk(relaxed = true)
        every { propertySetterFactory.create(any()) } returns Resolution.Claimed(propertySetter1, propertySetter2)

        adjustAnalyticsTrackerService.set(property)

        verify { propertySetterFactory.create(property) }
        coVerify { propertySetter1.set() }
        coVerify { propertySetter2.set() }
    }
}
