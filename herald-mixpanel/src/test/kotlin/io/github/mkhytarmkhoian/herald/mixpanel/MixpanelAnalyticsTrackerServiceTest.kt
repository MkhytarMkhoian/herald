package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import io.github.mkhytarmkhoian.herald.Resolution

class MixpanelAnalyticsTrackerServiceTest {

    private val eventTrackerFactory: MixpanelEventTrackerFactory = mockk(relaxed = true)
    private val propertySetterFactory: MixpanelPropertySetterFactory = mockk(relaxed = true)

    private val analyticsTrackerService = MixpanelAnalyticsTrackerService(
        eventTrackerFactory = eventTrackerFactory,
        propertySetterFactory = propertySetterFactory
    )

    @Before
    fun setup() {
        mockkStatic(MixpanelAPI::trackMap)
    }

    @After
    fun tearDown() {
        unmockkStatic(MixpanelAPI::trackMap)
    }

    @Test
    fun `On track event should map and track all events`() = runTest {
        val eventTracker1: MixpanelEventTracker = mockk(relaxed = true)
        val eventTracker2: MixpanelEventTracker = mockk(relaxed = true)
        val eventTracker3: MixpanelEventTracker = mockk(relaxed = true)
        val event: Event = mockk()
        every { eventTrackerFactory.create(any()) } returns Resolution.Claimed(
            listOf(
                eventTracker1,
                eventTracker2,
                eventTracker3
            ),
        )

        analyticsTrackerService.track(event)

        verify { eventTrackerFactory.create(event) }
        coVerify { eventTracker1.track() }
        coVerify { eventTracker2.track() }
        coVerify { eventTracker3.track() }
    }

    @Test
    fun `On set property should map and set all properties`() = runTest {
        val propertySetter1 = mockk<MixpanelPropertySetter>(relaxed = true)
        val propertySetter2 = mockk<MixpanelPropertySetter>(relaxed = true)
        val propertySetter3 = mockk<MixpanelPropertySetter>(relaxed = true)
        val property: Property = mockk()
        every { propertySetterFactory.create(any()) } returns Resolution.Claimed(
            listOf(
                propertySetter1,
                propertySetter2,
                propertySetter3
            ),
        )

        analyticsTrackerService.set(property)

        verify { propertySetterFactory.create(property) }
        coVerify { propertySetter1.set() }
        coVerify { propertySetter2.set() }
        coVerify { propertySetter3.set() }
    }
}
