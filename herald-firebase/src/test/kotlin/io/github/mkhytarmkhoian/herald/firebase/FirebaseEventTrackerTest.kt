package io.github.mkhytarmkhoian.herald.firebase

import android.os.Bundle
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class FirebaseEventTrackerTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putDouble(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun `toBundle should write integers as integers and reals as reals`() {
        mapOf<String, AnalyticsValue>(
            "plan" to AnalyticsValue.String("pro"),
            "seats" to AnalyticsValue.Int(3),
            "watched_ms" to AnalyticsValue.Long(4L),
            "ratio" to AnalyticsValue.Float(0.5f),
            "price" to AnalyticsValue.Double(9.99),
            "trial" to AnalyticsValue.Boolean(true),
        ).toBundle()

        verify { anyConstructed<Bundle>().putLong("seats", 3L) }
        verify { anyConstructed<Bundle>().putLong("watched_ms", 4L) }
        verify { anyConstructed<Bundle>().putDouble("ratio", 0.5) }
        verify { anyConstructed<Bundle>().putDouble("price", 9.99) }
        verify { anyConstructed<Bundle>().putString("plan", "pro") }
        verify { anyConstructed<Bundle>().putString("trial", "true") }
    }

    @Test
    fun `toBundle should write a float as the decimal it was declared as, not as its widening`() {
        mapOf<String, AnalyticsValue>(
            "price" to AnalyticsValue.Float(9.99f),
            "pi" to AnalyticsValue.Float(3.14f),
        ).toBundle()

        // 9.99f.toDouble() is 9.989999771118164, which is not what the event declared and not what
        // the string-only adapters send for the same parameter.
        verify { anyConstructed<Bundle>().putDouble("price", 9.99) }
        verify { anyConstructed<Bundle>().putDouble("pi", 3.14) }
    }
}
