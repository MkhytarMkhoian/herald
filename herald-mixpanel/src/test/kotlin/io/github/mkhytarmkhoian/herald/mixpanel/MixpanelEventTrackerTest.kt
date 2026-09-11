package io.github.mkhytarmkhoian.herald.mixpanel

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import org.junit.Test
import kotlin.test.assertEquals

internal class MixpanelEventTrackerTest {

    @Test
    fun `toMixpanelProperties should keep each value as its own type, not as text`() {
        val properties = mapOf(
            "plan" to AnalyticsValue.String("pro"),
            "seats" to AnalyticsValue.Int(3),
            "watched_ms" to AnalyticsValue.Long(4L),
            "ratio" to AnalyticsValue.Float(0.5f),
            "price" to AnalyticsValue.Double(9.99),
            "trial" to AnalyticsValue.Boolean(false),
        ).toMixpanelProperties()

        assertEquals(
            mapOf<String, Any>(
                "plan" to "pro",
                "seats" to 3,
                "watched_ms" to 4L,
                "ratio" to 0.5f,
                "price" to 9.99,
                "trial" to false,
            ),
            properties,
        )
    }
}
