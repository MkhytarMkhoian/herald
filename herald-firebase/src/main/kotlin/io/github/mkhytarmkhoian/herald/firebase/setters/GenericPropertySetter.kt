package io.github.mkhytarmkhoian.herald.firebase.setters

import io.github.mkhytarmkhoian.herald.firebase.FirebasePropertySetter
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.asString
import com.google.firebase.analytics.FirebaseAnalytics

public class GenericPropertySetter(
    private val property: Property,
    private val firebaseAnalytics: FirebaseAnalytics
) : FirebasePropertySetter {

    override suspend fun set() {
        firebaseAnalytics.setUserProperty(property.name, property.value.asString)
    }
}
