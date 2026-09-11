package io.github.mkhytarmkhoian.herald.mixpanel.setters

import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.mixpanel.android.mpmetrics.SuperPropertyUpdate
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.json.JSONObject
import org.junit.Test

class GenericPropertySetterTest {

    private val mixpanel: MixpanelAPI = mockk(relaxed = true)

    private fun updatedProperties(setter: GenericPropertySetter): JSONObject {
        setter.set()

        val updateSlot = slot<SuperPropertyUpdate>()
        verify { mixpanel.updateSuperProperties(capture(updateSlot)) }
        return mockk<JSONObject>(relaxed = true).also { updateSlot.captured.update(it) }
    }

    @Test
    fun `On set should update super properties on mixpanel`() {
        val property = property("Property Name", AnalyticsValue.String("Property Value"))

        val properties = updatedProperties(GenericPropertySetter(property, mixpanel))

        verify { properties.put("Property Name", "Property Value" as Any) }
    }

    @Test
    fun `On set should keep a numeric property numeric, so mixpanel can filter on it`() {
        val property = property("total_purchases", AnalyticsValue.Int(42))

        val properties = updatedProperties(GenericPropertySetter(property, mixpanel))

        verify { properties.put("total_purchases", 42 as Any) }
    }
}
