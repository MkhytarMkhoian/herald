package io.github.mkhytarmkhoian.herald.amplitude

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AmplitudeAnalyticsTrackerServiceTest {

    private val eventTrackerFactory: AmplitudeEventTrackerFactory = mockk()
    private val propertySetterFactory: AmplitudePropertySetterFactory = mockk()

    private val service = AmplitudeAnalyticsTrackerService(eventTrackerFactory, propertySetterFactory)

    @Test
    fun `On track should run every handler the chain returned`() = runTest {
        val first: AmplitudeEventTracker = mockk(relaxed = true)
        val second: AmplitudeEventTracker = mockk(relaxed = true)
        every { eventTrackerFactory.create(any()) } returns Resolution.Claimed(first, second)

        service.track(mockk<Event>())

        coVerify { first.track() }
        coVerify { second.track() }
    }

    @Test
    fun `On set should run every handler the chain returned`() = runTest {
        val setter: AmplitudePropertySetter = mockk(relaxed = true)
        every { propertySetterFactory.create(any()) } returns Resolution.Claimed(setter)

        service.set(mockk<Property>())

        coVerify { setter.set() }
    }

    @Test
    fun `On track a declined event should send nothing`() = runTest {
        every { eventTrackerFactory.create(any()) } returns Resolution.Declined

        service.track(mockk<Event>())
    }
}
