package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals

internal class AppsFlyerValuesTest {

    @Test
    fun `toAppsFlyerEventValues should keep each value as its own type, not as text`() {
        val values = mapOf(
            "plan" to AnalyticsValue.String("pro"),
            "seats" to AnalyticsValue.Int(3),
            "watched_ms" to AnalyticsValue.Long(4L),
            "ratio" to AnalyticsValue.Float(0.5f),
            "af_revenue" to AnalyticsValue.Double(9.99),
            "trial" to AnalyticsValue.Boolean(false),
        ).toAppsFlyerEventValues()

        assertEquals(
            mapOf<String, Any>(
                "plan" to "pro",
                "seats" to 3,
                "watched_ms" to 4L,
                "ratio" to 0.5f,
                "af_revenue" to 9.99,
                "trial" to false,
            ),
            values,
        )
    }
}
