package io.github.mkhytarmkhoian.herald

import org.junit.Test
import kotlin.test.assertEquals

class AnalyticsValueTest {

    @Test
    fun `asString should keep the distinction the variant was chosen for`() {
        assertEquals("3", AnalyticsValue.Int(3).asString)
        assertEquals("3", AnalyticsValue.Long(3L).asString)
        assertEquals("0.5", AnalyticsValue.Float(0.5f).asString)
        assertEquals("9.99", AnalyticsValue.Float(9.99f).asString)
        assertEquals("3.0", AnalyticsValue.Double(3.0).asString)
        assertEquals("9.99", AnalyticsValue.Double(9.99).asString)
    }

    @Test
    fun `asString should pass text through and spell a boolean out`() {
        assertEquals("pro", AnalyticsValue.String("pro").asString)
        assertEquals("true", AnalyticsValue.Boolean(true).asString)
    }

    @Test
    fun `parameters should type each value by the overload it was written with`() {
        val parameters = parameters {
            put("plan", "pro")
            put("seats", 3)
            put("watched_ms", 4L)
            put("ratio", 0.5f)
            put("price", 9.99)
            put("trial", false)
        }

        assertEquals(
            mapOf(
                "plan" to AnalyticsValue.String("pro"),
                "seats" to AnalyticsValue.Int(3),
                "watched_ms" to AnalyticsValue.Long(4L),
                "ratio" to AnalyticsValue.Float(0.5f),
                "price" to AnalyticsValue.Double(9.99),
                "trial" to AnalyticsValue.Boolean(false),
            ),
            parameters,
        )
    }

    @Test
    fun `parameters should be empty by default on an event that declares none`() {
        val event = object : Event {
            override val name = "an_event"
        }

        assertEquals(emptyMap(), event.parameters)
    }
}
