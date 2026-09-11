package io.github.mkhytarmkhoian.herald.log.setters

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.log.RecordingLogger
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GenericLogPropertySetterTest {

    private val logger = RecordingLogger()

    private fun property(propertyName: String, propertyValue: AnalyticsValue) = object : Property {
        override val name = propertyName
        override val value = propertyValue
    }

    @Test
    fun `A property prints its name and value on one line`() = runTest {
        GenericLogPropertySetter(property("total_purchases", AnalyticsValue.Int(42)), logger).set()

        assertEquals(listOf("[herald] prop    total_purchases = 42"), logger.messages)
    }

    @Test
    fun `A string property prints without quoting, the same as an event parameter would`() = runTest {
        GenericLogPropertySetter(property("plan", AnalyticsValue.String("pro")), logger).set()

        assertEquals(listOf("[herald] prop    plan = pro"), logger.messages)
    }
}
