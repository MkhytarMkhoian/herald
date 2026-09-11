package io.github.mkhytarmkhoian.herald.mixpanel.setters

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelPropertySetter
import io.github.mkhytarmkhoian.herald.mixpanel.asMixpanelValue
import io.github.mkhytarmkhoian.herald.Property

public class GenericPropertySetter(
    private val property: Property,
    private val mixpanel: MixpanelAPI
) : MixpanelPropertySetter {

    override fun set() {
        mixpanel.updateSuperProperties { properties ->
            properties.put(property.name, property.value.asMixpanelValue)
            properties
        }
    }
}
