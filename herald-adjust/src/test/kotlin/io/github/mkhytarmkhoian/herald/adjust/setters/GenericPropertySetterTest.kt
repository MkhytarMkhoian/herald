package io.github.mkhytarmkhoian.herald.adjust.setters

import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Property
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class GenericPropertySetterTest {

    private val adjust: AdjustInstance = mockk(relaxed = true)

    /**
     * A real [Property] rather than a mock: `value` returns a `@JvmInline value class`, and
     * stubbing such a getter erases the type and fails the cast inside mockk.
     */
    private fun property(propertyName: String, propertyValue: AnalyticsValue) = object : Property {
        override val name = propertyName
        override val value = propertyValue
    }

    @Test
    fun `On set should register the property as a global callback parameter`() = runTest {
        GenericPropertySetter(property("plan", AnalyticsValue.String("pro")), adjust).set()

        verify { adjust.addGlobalCallbackParameter("plan", "pro") }
    }

    @Test
    fun `On set should flatten a numeric property, adjust callback parameters being text only`() = runTest {
        GenericPropertySetter(property("total_purchases", AnalyticsValue.Int(42)), adjust).set()

        verify { adjust.addGlobalCallbackParameter("total_purchases", "42") }
    }
}
