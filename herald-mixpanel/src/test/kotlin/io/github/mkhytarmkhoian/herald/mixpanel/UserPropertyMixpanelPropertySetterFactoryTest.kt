package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UserProperty
import io.github.mkhytarmkhoian.herald.mixpanel.setters.GenericPropertySetter
import io.github.mkhytarmkhoian.herald.mixpanel.setters.UserPropertySetter
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import io.github.mkhytarmkhoian.herald.Resolution

class UserPropertyMixpanelPropertySetterFactoryTest {

    private val mixpanel: MixpanelAPI = mockk()

    private val propertySetterFactory = UserPropertyMixpanelPropertySetterFactory(mixpanel)

    @Test
    fun `On create with a UserProperty should return UserPropertySetter, which writes the people profile`() {
        val property: UserProperty = mockk()

        val propertySetters = propertySetterFactory.create(property).handlers

        assertEquals(1, propertySetters.size)
        assertIs<UserPropertySetter>(propertySetters.first())
    }

    @Test
    fun `On create with a UserProperty should not also register a super property`() {
        val property: UserProperty = mockk()

        val propertySetters = propertySetterFactory.create(property).handlers

        assertEquals(0, propertySetters.filterIsInstance<GenericPropertySetter>().size)
    }

    @Test
    fun `On create with a plain Property should decline it`() {
        val property: Property = mockk()

        assertEquals(Resolution.Declined, propertySetterFactory.create(property))
    }

    @Test
    fun `A chain with this factory first sends a UserProperty to the profile, not to super properties`() {
        val chain = CompositeMixpanelPropertySetterFactory(
            propertySetterFactory,
            GenericMixpanelPropertySetterFactory(mixpanel),
        )
        val property: UserProperty = mockk()

        val propertySetters = chain.create(property).handlers

        assertEquals(1, propertySetters.size)
        assertIs<UserPropertySetter>(propertySetters.first())
    }
}
