package io.github.mkhytarmkhoian.herald.mixpanel.setters

import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.mixpanel.android.mpmetrics.MixpanelAPI.People
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class UserPropertySetterTest {

    private val peopleMock: People = mockk(relaxed = true)
    private val mixpanel: MixpanelAPI = mockk(relaxed = true) {
        every { people } returns peopleMock
    }

    @Test
    fun `On set should set property through people on mixpanel`() {
        val property = property("Property Name", AnalyticsValue.String("Property Value"))

        UserPropertySetter(property, mixpanel).set()

        verify { mixpanel.people }
        verify { peopleMock.set("Property Name", "Property Value") }
    }

    @Test
    fun `On set should keep a numeric property numeric, so mixpanel can filter on it`() {
        val property = property("total_purchases", AnalyticsValue.Int(42))

        UserPropertySetter(property, mixpanel).set()

        verify { peopleMock.set("total_purchases", 42) }
    }
}
