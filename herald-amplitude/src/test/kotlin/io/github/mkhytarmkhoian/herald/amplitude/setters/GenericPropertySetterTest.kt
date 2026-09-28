package io.github.mkhytarmkhoian.herald.amplitude.setters

import com.amplitude.android.Amplitude
import com.amplitude.core.events.Identify
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Property
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class GenericPropertySetterTest {

    private val amplitude: Amplitude = mockk(relaxed = true)

    // A real Property rather than a mock: `value` returns a value class, and stubbing such a getter
    // fails the cast inside mockk.
    private fun property(propertyName: String, propertyValue: AnalyticsValue) = object : Property {
        override val name = propertyName
        override val value = propertyValue
    }

    private suspend fun identified(property: Property): Map<String, Any?> {
        GenericPropertySetter(property, amplitude).set()

        val identify = slot<Identify>()
        verify { amplitude.identify(capture(identify)) }
        return identify.captured.properties
    }

    @Test
    fun `On set should set the property as a user property`() = runTest {
        val properties = identified(property("plan", AnalyticsValue.String("pro")))

        assertEquals(mapOf("\$set" to mapOf("plan" to "pro")), properties)
    }

    @Test
    fun `On set should keep a numeric property numeric, so amplitude can filter on it`() = runTest {
        val properties = identified(property("total_purchases", AnalyticsValue.Int(42)))

        assertEquals(mapOf("\$set" to mapOf("total_purchases" to 42)), properties)
    }
}
