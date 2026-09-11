package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UserProperty
import io.github.mkhytarmkhoian.herald.adjust.setters.GenericPropertySetter
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GenericAdjustPropertySetterFactoryTest {

    private val adjust: AdjustInstance = mockk()

    private val propertySetterFactory = GenericAdjustPropertySetterFactory(adjust)

    @Test
    fun `On create should return GenericPropertySetter`() {
        val property: Property = mockk()

        val propertySetters = propertySetterFactory.create(property).handlers

        assertEquals(1, propertySetters.size)
        assertIs<GenericPropertySetter>(propertySetters.first())
    }

    @Test
    fun `On create should treat a UserProperty the same, adjust not separating the two`() {
        val property: UserProperty = mockk()

        val propertySetters = propertySetterFactory.create(property).handlers

        assertEquals(1, propertySetters.size)
        assertIs<GenericPropertySetter>(propertySetters.first())
    }
}
