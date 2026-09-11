package io.github.mkhytarmkhoian.herald.adjust.setters

import io.github.mkhytarmkhoian.herald.adjust.AdjustPropertySetter
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.asString
import com.adjust.sdk.AdjustInstance

public class GenericPropertySetter(
    private val property: Property,
    private val adjust: AdjustInstance,
) : AdjustPropertySetter {

    override suspend fun set() {
        adjust.addGlobalCallbackParameter(property.name, property.value.asString)
    }
}
