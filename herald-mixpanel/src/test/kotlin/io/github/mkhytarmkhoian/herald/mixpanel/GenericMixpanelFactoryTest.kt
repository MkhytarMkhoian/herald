package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.UserProperty
import io.github.mkhytarmkhoian.herald.mixpanel.setters.GenericPropertySetter
import io.github.mkhytarmkhoian.herald.mixpanel.trackers.GenericEventTracker
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GenericMixpanelFactoryTest {

    private val mixpanel: MixpanelAPI = mockk()

    private val eventTrackerFactory = GenericMixpanelEventTrackerFactory(mixpanel)
    private val propertySetterFactory = GenericMixpanelPropertySetterFactory(mixpanel)

    @Test
    fun `On create should return GenericEventTracker for an unmarked event`() {
        val event: Event = mockk()

        val eventTrackers = eventTrackerFactory.create(event).handlers

        assertEquals(1, eventTrackers.size)
        assertIs<GenericEventTracker>(eventTrackers.first())
    }

    @Test
    fun `On create should claim a marked event too, so that it only belongs last in a chain`() {
        val event: ScreenViewEvent = mockk()

        val eventTrackers = eventTrackerFactory.create(event).handlers

        assertEquals(1, eventTrackers.size)
        assertIs<GenericEventTracker>(eventTrackers.first())
    }

    @Test
    fun `On create should return GenericPropertySetter`() {
        val property: Property = mockk()

        val propertySetters = propertySetterFactory.create(property).handlers

        assertEquals(1, propertySetters.size)
        assertIs<GenericPropertySetter>(propertySetters.first())
    }

    @Test
    fun `On create should claim a UserProperty too, which is why it belongs last`() {
        val property: UserProperty = mockk()

        val propertySetters = propertySetterFactory.create(property).handlers

        assertEquals(1, propertySetters.size)
        assertIs<GenericPropertySetter>(propertySetters.first())
    }
}
