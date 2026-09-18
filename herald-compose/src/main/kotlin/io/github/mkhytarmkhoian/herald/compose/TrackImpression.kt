package io.github.mkhytarmkhoian.herald.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.mkhytarmkhoian.herald.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Tracks [event] once, when at least [threshold] of this layout is visible on screen. With a
 * [minVisibleDuration], it has to stay that visible for that long first; leaving earlier cancels
 * the count.
 *
 * "Visible" means inside the window after clipping by parents: half of a list item under the app
 * bar is half visible. Something drawn over the layout, such as a dialog, does not count as
 * hiding it.
 *
 * It fires once per appearance. A list item that scrolls off and back on counts again. If the
 * same item should count only once per session, keep that rule in the caller.
 *
 * Use a `data class` for a parameterised [event], or `remember` it. A new, unequal event
 * restarts the count.
 *
 * Does nothing in previews and in the layout inspector.
 */
public fun Modifier.trackImpression(
    event: Event,
    threshold: Float = 0.5f,
    minVisibleDuration: Duration = Duration.ZERO,
): Modifier {
    require(threshold in 0f..1f) { "threshold must be within 0..1, was $threshold" }
    return this then ImpressionElement(event, threshold, minVisibleDuration)
}

private data class ImpressionElement(
    val event: Event,
    val threshold: Float,
    val minVisibleDuration: Duration,
) : ModifierNodeElement<ImpressionNode>() {

    override fun create(): ImpressionNode = ImpressionNode(event, threshold, minVisibleDuration)

    override fun update(node: ImpressionNode) {
        node.update(event, threshold, minVisibleDuration)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "trackImpression"
        properties["event"] = event.name
        properties["threshold"] = threshold
        properties["minVisibleDuration"] = minVisibleDuration
    }
}

private class ImpressionNode(
    private var event: Event,
    private var threshold: Float,
    private var minVisibleDuration: Duration,
) : Modifier.Node(), GlobalPositionAwareModifierNode, CompositionLocalConsumerModifierNode {

    private var tracked = false
    private var pending: Job? = null

    fun update(event: Event, threshold: Float, minVisibleDuration: Duration) {
        if (event != this.event) {
            tracked = false
            cancelPending()
        }
        this.event = event
        this.threshold = threshold
        this.minVisibleDuration = minVisibleDuration
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (tracked || currentValueOf(LocalInspectionMode)) return
        val visible = coordinates.visibleFraction() >= threshold
        when {
            visible && pending == null -> pending = coroutineScope.launch {
                delay(minVisibleDuration)
                tracked = true
                pending = null
                currentValueOf(LocalEventTrackerService).track(event)
            }

            !visible -> cancelPending()
        }
    }

    // Lazy layouts reuse and re-attach item nodes when items scroll; each is a new appearance.
    override fun onReset() {
        tracked = false
        cancelPending()
    }

    override fun onAttach() {
        tracked = false
    }

    override fun onDetach() {
        cancelPending()
    }

    private fun cancelPending() {
        pending?.cancel()
        pending = null
    }
}

private fun LayoutCoordinates.visibleFraction(): Float {
    if (!isAttached) return 0f
    val area = size.width.toFloat() * size.height
    if (area == 0f) return 0f
    val visible = boundsInWindow()
    return (visible.width * visible.height) / area
}
