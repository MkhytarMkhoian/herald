package io.github.mkhytarmkhoian.herald.mixpanel.trackers

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.mixpanel.MixpanelEventTracker
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.mixpanel.toMixpanelProperties

public class GenericEventTracker(
    private val event: Event,
    private val mixpanel: MixpanelAPI,
) : MixpanelEventTracker {

    override suspend fun track() {
        mixpanel.trackMap(event.name, event.parameters.toMixpanelProperties())
    }
}
