package io.github.mkhytarmkhoian.herald.firebase.trackers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class GenericEventTrackerTest {

    private val event: Event = object : Event {
        override val name = "checkout_started"
        override val parameters = mapOf<String, AnalyticsValue>("plan" to AnalyticsValue.String("pro"))
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
    fun `On track should log the event under its own name with its parameters`() = runTest {
        GenericEventTracker(event, firebaseAnalytics).track()

        verify { anyConstructed<Bundle>().putString("plan", "pro") }
        verify { firebaseAnalytics.logEvent("checkout_started", any<Bundle>()) }
    }
}
