package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException
import io.github.mkhytarmkhoian.herald.UserProperty
import io.github.mkhytarmkhoian.herald.amplitude.setters.GenericPropertySetter
import io.github.mkhytarmkhoian.herald.amplitude.trackers.GenericEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.trackers.RevenueEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.trackers.ScreenViewEventTracker
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class AmplitudeFactoriesTest {

    private val amplitude: Amplitude = mockk()

    @Test
    fun `Generic event factory should claim any event, marked or not`() {
        val factory = GenericAmplitudeEventTrackerFactory(amplitude)

        assertIs<GenericEventTracker>(factory.create(mockk<Event>()).handlers.single())
        assertIs<GenericEventTracker>(factory.create(mockk<ScreenViewEvent>()).handlers.single())
    }

    @Test
    fun `Screen view factory should claim a ScreenViewEvent and decline the rest`() {
        val factory = ScreenViewAmplitudeEventTrackerFactory(amplitude)

        assertIs<ScreenViewEventTracker>(factory.create(mockk<ScreenViewEvent>()).handlers.single())
        assertEquals(Resolution.Declined, factory.create(mockk<Event>()))
    }

    @Test
    fun `Revenue factory should claim a RevenueEvent and decline the rest`() {
        val factory = RevenueAmplitudeEventTrackerFactory(amplitude)

        assertIs<RevenueEventTracker>(factory.create(mockk<RevenueEvent>()).handlers.single())
        assertEquals(Resolution.Declined, factory.create(mockk<Event>()))
    }

    @Test
    fun `Generic property factory should claim a UserProperty too, since Amplitude has one store`() {
        val factory = GenericAmplitudePropertySetterFactory(amplitude)

        assertIs<GenericPropertySetter>(factory.create(mockk<Property>()).handlers.single())
        assertIs<GenericPropertySetter>(factory.create(mockk<UserProperty>()).handlers.single())
    }

    @Test
    fun `RequireMapped factories should throw, naming what was unmapped`() {
        val event: Event = mockk { every { name } returns "unmapped" }
        val property: Property = mockk { every { name } returns "unmapped" }

        val eventFailure = assertFailsWith<UnhandledEventException> {
            RequireMappedAmplitudeEventTrackerFactory.create(event)
        }
        val propertyFailure = assertFailsWith<UnhandledPropertyException> {
            RequireMappedAmplitudePropertySetterFactory.create(property)
        }

        assertSame(event, eventFailure.event)
        assertSame(property, propertyFailure.property)
    }
}
