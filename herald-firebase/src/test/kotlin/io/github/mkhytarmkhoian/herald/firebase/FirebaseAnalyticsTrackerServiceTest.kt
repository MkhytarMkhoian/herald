package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import io.github.mkhytarmkhoian.herald.Resolution

class FirebaseAnalyticsTrackerServiceTest {

    private val eventTrackerFactory: FirebaseEventTrackerFactory = mockk()
    private val propertySetterFactory: FirebasePropertySetterFactory = mockk()

    private val analyticsTrackerService = FirebaseAnalyticsTrackerService(eventTrackerFactory, propertySetterFactory)

    @Test
    fun `On track should create correct event tracker and track it`() = runTest {
        val event: Event = mockk()
        val eventTracker: FirebaseEventTracker = mockk(relaxed = true)
        every { eventTrackerFactory.create(any()) } returns Resolution.Claimed(eventTracker)

        analyticsTrackerService.track(event)

        verify { eventTrackerFactory.create(event) }
        coVerify { eventTracker.track() }
    }

    @Test
    fun `On set should create correct property setter and set it`() = runTest {
        val property: Property = mockk()
        val propertySetter: FirebasePropertySetter = mockk(relaxed = true)
        every { propertySetterFactory.create(any()) } returns Resolution.Claimed(propertySetter)

        analyticsTrackerService.set(property)

        verify { propertySetterFactory.create(property) }
        coVerify { propertySetter.set() }
    }

    @Test
    fun `On track should do nothing when no factory claims the event`() = runTest {
        val event: Event = mockk()
        every { eventTrackerFactory.create(any()) } returns Resolution.Declined

        analyticsTrackerService.track(event)

        verify { eventTrackerFactory.create(event) }
    }

    @Test
    fun `On set should do nothing when no factory claims the property`() = runTest {
        val property: Property = mockk()
        every { propertySetterFactory.create(any()) } returns Resolution.Declined

        analyticsTrackerService.set(property)

        verify { propertySetterFactory.create(property) }
    }
}
