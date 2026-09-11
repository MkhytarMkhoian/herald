package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.mixpanel.setters.GenericPropertySetter

/**
 * Registers any property as a Mixpanel super property, so it rides along on every event sent
 * afterwards. Claims everything, so it belongs last in a chain — and after
 * [UserPropertyMixpanelPropertySetterFactory], which would otherwise never see a `UserProperty`.
 */
public class GenericMixpanelPropertySetterFactory(
    private val mixpanel: MixpanelAPI,
) : MixpanelPropertySetterFactory {

    override fun create(property: Property): Resolution<MixpanelPropertySetter> =
        Resolution.Claimed(GenericPropertySetter(property, mixpanel))
}
