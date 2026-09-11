package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class RequireMappedAdjustFactoriesTest {

    @Test
    fun `On create should throw for any event, naming it`() {
        val event: Event = mockk { every { name } returns "unmapped" }

        val failure = assertFailsWith<UnhandledEventException> {
            RequireMappedAdjustEventTrackerFactory.create(event)
        }

        assertSame(event, failure.event)
    }

    @Test
    fun `On create should throw for any property, naming it`() {
        val property: Property = mockk { every { name } returns "unmapped" }

        val failure = assertFailsWith<UnhandledPropertyException> {
            RequireMappedAdjustPropertySetterFactory.create(property)
        }

        assertSame(property, failure.property)
    }
}
