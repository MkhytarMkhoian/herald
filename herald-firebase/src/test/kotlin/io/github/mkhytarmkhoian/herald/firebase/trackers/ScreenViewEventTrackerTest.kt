package io.github.mkhytarmkhoian.herald.firebase.trackers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import io.mockk.verifyOrder
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScreenViewEventTrackerTest {

    private val event: ScreenViewEvent = object : ScreenViewEvent {
        override val name = "Checkout"
        override val parameters = mapOf<String, AnalyticsValue>("tab" to AnalyticsValue.String("cart"))
    }
    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)

    @Before
    fun setUp() {
        // Bundle is an Android stub here; intercept its writes so the real toBundle() can run.
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun `On track should log screen_view with the event's name as screen_name`() = runTest {
        ScreenViewEventTracker(event, firebaseAnalytics).track()

        verifyOrder {
            anyConstructed<Bundle>().putString("tab", "cart")
            anyConstructed<Bundle>().putString(FirebaseAnalytics.Param.SCREEN_NAME, "Checkout")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, any<Bundle>())
        }
        verify(inverse = true) { firebaseAnalytics.logEvent("Checkout", any<Bundle>()) }
    }

    @Test
    fun `On track with its own screen_name parameter should throw and log nothing`() = runTest {
        val clashing = object : ScreenViewEvent {
            override val name = "Checkout"
            override val parameters = mapOf<String, AnalyticsValue>(
                FirebaseAnalytics.Param.SCREEN_NAME to AnalyticsValue.String("Other"),
            )
        }

        assertFailsWith<IllegalArgumentException> {
            ScreenViewEventTracker(clashing, firebaseAnalytics).track()
        }
        verify { firebaseAnalytics wasNot Called }
    }
}
