package io.github.mkhytarmkhoian.herald.firebase.trackers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScreenViewEventTrackerTest {

    private val event: ScreenViewEvent = object : ScreenViewEvent {
        override val name = "screen_shown"
        override val screenName = "Checkout"
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
    fun `On track should log the reserved screen_view event, not the event's own name`() = runTest {
        ScreenViewEventTracker(event, firebaseAnalytics).track()

        verifyOrder {
            anyConstructed<Bundle>().putString("tab", "cart")
            anyConstructed<Bundle>().putString(FirebaseAnalytics.Param.SCREEN_NAME, "Checkout")
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, any<Bundle>())
        }
        verify(inverse = true) { firebaseAnalytics.logEvent("screen_shown", any<Bundle>()) }
    }
}
