package io.github.mkhytarmkhoian.herald.amplitude.trackers

import com.amplitude.android.Amplitude
import com.amplitude.core.events.EventOptions
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudeEventTracker
import io.github.mkhytarmkhoian.herald.amplitude.RevenueEvent
import io.github.mkhytarmkhoian.herald.amplitude.toAmplitudeRevenue

/** Sends one [RevenueEvent] through Amplitude's revenue API. */
public class RevenueEventTracker(
    private val event: RevenueEvent,
    private val amplitude: Amplitude,
) : AmplitudeEventTracker {

    override suspend fun track() {
        // Amplitude's Revenue has no insert id; it travels on the options of the call instead.
        val options = event.insertId?.let { id -> EventOptions().apply { insertId = id } }
        amplitude.revenue(event.toAmplitudeRevenue(), options)
    }
}
