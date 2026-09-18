package io.github.mkhytarmkhoian.herald.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.firebase.setters.GenericPropertySetter

/**
 * Sets any property as a Firebase user property. Firebase draws no distinction between session
 * and profile attributes, so this covers `UserProperty` too and no marker-aware factory is needed.
 * Claims everything, so it belongs last in a chain.
 */
public class GenericFirebasePropertySetterFactory(
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebasePropertySetterFactory, FallbackFactory {

    override fun create(property: Property): Resolution<FirebasePropertySetter> =
        Resolution.Claimed(GenericPropertySetter(property, firebaseAnalytics))
}
