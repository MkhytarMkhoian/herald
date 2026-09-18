package io.github.mkhytarmkhoian.herald.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.testing.FakeAnalyticsProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TrackTest {

    @get:Rule
    val compose = createComposeRule()

    private val analytics = FakeAnalyticsProvider()

    private object HomeShown : ScreenViewEvent {
        override val name = "home_shown"
        override val screenName = "Home"
    }

    private object BannerClicked : Event {
        override val name = "banner_clicked"
    }

    @Test
    fun `a screen view is tracked when the host resumes`() {
        compose.setContent {
            CompositionLocalProvider(LocalEventTrackerService provides analytics) {
                TrackScreenView(HomeShown)
            }
        }
        compose.waitForIdle()

        analytics.assertTracked("home_shown")
        analytics.assertNothingElseTracked()
    }

    @Test
    fun `rememberTracker tracks each call`() {
        compose.setContent {
            CompositionLocalProvider(LocalEventTrackerService provides analytics) {
                val track = rememberTracker()
                Button(onClick = { track(BannerClicked) }, modifier = Modifier.testTag("banner")) { Box(Modifier) }
            }
        }

        compose.onNodeWithTag("banner").performClick()
        compose.onNodeWithTag("banner").performClick()
        compose.waitForIdle()

        analytics.assertTrackedTimes("banner_clicked", 2)
    }

    @Test
    fun `in inspection mode nothing is tracked and no provider is needed`() {
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                TrackScreenView(HomeShown)
                val track = rememberTracker()
                Button(onClick = { track(BannerClicked) }, modifier = Modifier.testTag("banner")) { Box(Modifier) }
            }
        }
        compose.onNodeWithTag("banner").performClick()
        compose.waitForIdle()

        assertEquals(emptyList(), analytics.records)
    }
}
