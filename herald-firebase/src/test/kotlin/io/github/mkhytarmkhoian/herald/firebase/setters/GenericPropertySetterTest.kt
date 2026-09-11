package io.github.mkhytarmkhoian.herald.firebase.setters

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Property
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GenericPropertySetterTest {

    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)

    /**
     * A real [Property] rather than a mock: `value` returns a `@JvmInline value class`, and
     * stubbing such a getter erases the type and fails the cast inside mockk.
     */
    private fun property(propertyName: String, propertyValue: AnalyticsValue) = object : Property {
        override val name = propertyName
        override val value = propertyValue
    }

    @Test
    fun `On set should set user property on firebase analytics`() = runTest {
        val property = property("Property Name", AnalyticsValue.String("Property Value"))

        GenericPropertySetter(property, firebaseAnalytics).set()

        verify { firebaseAnalytics.setUserProperty("Property Name", "Property Value") }
    }

    @Test
    fun `On set should flatten a numeric property, firebase user properties being text only`() = runTest {
        val property = property("total_purchases", AnalyticsValue.Int(42))

        GenericPropertySetter(property, firebaseAnalytics).set()

        verify { firebaseAnalytics.setUserProperty("total_purchases", "42") }
    }
}
