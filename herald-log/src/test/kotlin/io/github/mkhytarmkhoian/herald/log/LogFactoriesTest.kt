package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException
import io.github.mkhytarmkhoian.herald.log.setters.GenericLogPropertySetter
import io.github.mkhytarmkhoian.herald.log.trackers.GenericEventTracker
import io.github.mkhytarmkhoian.herald.log.trackers.ScreenViewEventTracker
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import io.github.mkhytarmkhoian.herald.Resolution

class LogFactoriesTest {

    private val logger = RecordingLogger()

    @Test
    fun `The screen-view factory claims a screen view and declines everything else`() {
        val factory = ScreenViewLogEventTrackerFactory(logger)

        val claimed = factory.create(mockk<ScreenViewEvent>()).handlers

        assertEquals(1, claimed.size)
        assertIs<ScreenViewEventTracker>(claimed.first())
        assertEquals(Resolution.Declined, factory.create(mockk<Event>()))
    }

    @Test
    fun `The generic factories claim everything, so they belong last`() {
        val eventTrackers = GenericLogEventTrackerFactory(logger).create(mockk<ScreenViewEvent>())
        val propertySetters = GenericLogPropertySetterFactory(logger).create(mockk<Property>())

        assertIs<GenericEventTracker>(eventTrackers.handlers.first())
        assertIs<GenericLogPropertySetter>(propertySetters.handlers.first())
    }

    @Test
    fun `The RequireMapped factories turn an unclaimed record into a failure`() {
        // The exception messages name the record, so these mocks need a name to report.
        val event: Event = mockk { every { name } returns "unwired_event" }
        val property: Property = mockk { every { name } returns "unwired_property" }

        assertFailsWith<UnhandledEventException> { RequireMappedLogEventTrackerFactory.create(event) }
        assertFailsWith<UnhandledPropertyException> { RequireMappedLogPropertySetterFactory.create(property) }
    }

    @Test
    fun `A chain takes the first factory that does not decline`() {
        val chain = CompositeLogEventTrackerFactory(
            ScreenViewLogEventTrackerFactory(logger),
            GenericLogEventTrackerFactory(logger),
        )

        assertIs<ScreenViewEventTracker>(chain.create(mockk<ScreenViewEvent>()).handlers.first())
        assertIs<GenericEventTracker>(chain.create(mockk<Event>()).handlers.first())
    }
}
