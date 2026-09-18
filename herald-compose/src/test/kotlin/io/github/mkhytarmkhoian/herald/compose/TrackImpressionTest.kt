package io.github.mkhytarmkhoian.herald.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.parameters
import io.github.mkhytarmkhoian.herald.testing.AnalyticsRecord
import io.github.mkhytarmkhoian.herald.testing.FakeAnalyticsProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TrackImpressionTest {

    @get:Rule
    val compose = createComposeRule()

    private val analytics = FakeAnalyticsProvider()

    private data class ItemImpression(val index: Int) : Event {
        override val name = "item_impression"
        override val parameters = parameters { put("index", index) }
    }

    private fun impressions(): List<Int> = analytics.records
        .filterIsInstance<AnalyticsRecord.Tracked>()
        .map { (it.event as ItemImpression).index }

    /** Twenty 100dp rows in a 500dp viewport: exactly five are fully visible at a time. */
    private fun setList(minVisibleDuration: Duration = Duration.ZERO) {
        compose.setContent {
            CompositionLocalProvider(LocalEventTrackerService provides analytics) {
                LazyColumn(modifier = Modifier
                    .height(500.dp)
                    .testTag("list")) {
                    items(count = 20, key = { it }) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .trackImpression(ItemImpression(index), minVisibleDuration = minVisibleDuration),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `only the rows inside the viewport are impressions, each once`() {
        setList()
        compose.waitForIdle()

        assertEquals(listOf(0, 1, 2, 3, 4), impressions().sorted())
    }

    @Test
    fun `scrolling reveals new rows, and a row that comes back counts again`() {
        setList()
        compose.waitForIdle()

        compose.onNodeWithTag("list").performScrollToIndex(10)
        compose.waitForIdle()
        val afterScroll = impressions()

        compose.onNodeWithTag("list").performScrollToIndex(0)
        compose.waitForIdle()

        assertEquals(listOf(10, 11, 12, 13, 14), afterScroll.filter { it >= 5 }.sorted())
        assertEquals(2, impressions().count { it == 0 })
    }

    /**
     * Only the "not yet" half of the duration rule is testable here: the Compose test harness runs
     * `delay` on its own scheduler and `waitForIdle` advances that scheduler until idle, so any
     * finite wait completes inside `setContent`. An infinite wait proves a row is not counted
     * before its time is up, and that scrolling it away leaves no impression behind.
     */
    @Test
    fun `a row is not an impression before the minimum visible duration has passed`() {
        setList(minVisibleDuration = Duration.INFINITE)
        compose.waitForIdle()

        compose.onNodeWithTag("list").performScrollToIndex(10)
        compose.waitForIdle()

        assertEquals(emptyList(), impressions())
    }
}
