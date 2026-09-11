package io.github.mkhytarmkhoian.herald.firebase.trackers

import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTracker
import io.github.mkhytarmkhoian.herald.firebase.toBundle
import io.github.mkhytarmkhoian.herald.Event
import com.google.firebase.analytics.FirebaseAnalytics

public class GenericEventTracker(
    private val event: Event,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {

    override suspend fun track() {
        val bundle = event.parameters.toBundle()
        firebaseAnalytics.logEvent(event.name, bundle)
    }
}
