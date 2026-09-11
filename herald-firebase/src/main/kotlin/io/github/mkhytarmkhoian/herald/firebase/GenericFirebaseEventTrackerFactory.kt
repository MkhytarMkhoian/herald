package io.github.mkhytarmkhoian.herald.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.firebase.trackers.GenericEventTracker

/**
 * Logs any event under its own name with its parameters attached. Claims everything, so it only
 * belongs last in a chain — anything after it is unreachable.
 */
public class GenericFirebaseEventTrackerFactory(
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTrackerFactory {

    override fun create(event: Event): Resolution<FirebaseEventTracker> =
        Resolution.Claimed(GenericEventTracker(event, firebaseAnalytics))
}
