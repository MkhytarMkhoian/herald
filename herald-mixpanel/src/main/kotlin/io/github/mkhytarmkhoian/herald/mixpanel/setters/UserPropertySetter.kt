package io.github.mkhytarmkhoian.herald.mixpanel.setters

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelPropertySetter
import io.github.mkhytarmkhoian.herald.mixpanel.asMixpanelValue
import io.github.mkhytarmkhoian.herald.Property

public class UserPropertySetter(
    private val property: Property,
    private val mixpanel: MixpanelAPI
) : MixpanelPropertySetter {

    override suspend fun set() {
        mixpanel.people.set(property.name, property.value.asMixpanelValue)
    }
}
