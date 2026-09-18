package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.adjust.setters.GenericPropertySetter

/**
 * Registers any property as an Adjust global callback parameter, appended to the callbacks Adjust
 * fires for later sessions and events. Claims everything, so it belongs last in a chain.
 *
 * Adjust draws no distinction between an attribute of the session and one of the person, so this
 * covers `UserProperty` too.
 *
 * There is no generic Adjust *event* factory: an event reaches Adjust only with a token.
 */
public class GenericAdjustPropertySetterFactory(
    private val adjust: AdjustInstance,
) : AdjustPropertySetterFactory, FallbackFactory {

    override fun create(property: Property): Resolution<AdjustPropertySetter> =
        Resolution.Claimed(GenericPropertySetter(property, adjust))
}
