package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UserProperty
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.mixpanel.setters.UserPropertySetter

/**
 * Stores a `UserProperty` on the Mixpanel people profile, and declines every other property.
 *
 * Mixpanel keeps two stores: `people.set` writes to the person's profile, while super properties
 * ride along on every event sent afterwards. `UserProperty` says which one an attribute belongs in.
 *
 * **Put this before [GenericMixpanelPropertySetterFactory] in the chain.** A `UserProperty` is also
 * a [Property], so a generic factory placed first claims it and the profile is never written to.
 *
 * This writes to the profile only. To also put it on every event, write a factory returning both
 * setters.
 */
public class UserPropertyMixpanelPropertySetterFactory(
    private val mixpanel: MixpanelAPI,
) : MixpanelPropertySetterFactory {

    override fun create(property: Property): Resolution<MixpanelPropertySetter> = when (property) {
        is UserProperty -> Resolution.Claimed(UserPropertySetter(property, mixpanel))
        else -> Resolution.Declined
    }
}
