package io.github.mkhytarmkhoian.herald.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.firebase.trackers.ScreenViewEventTracker

/**
 * Claims every [ScreenViewEvent] and declines everything else, which is
 * [GenericFirebaseEventTrackerFactory]'s business.
 *
 * A screen view cannot go through the generic path: GA4 models it as a reserved `screen_view`
 * event whose parameter is the screen name, not as an event under the app's own name.
 *
 * To handle screen views differently, write a factory and place it before this one in the chain.
 */
public class ScreenViewFirebaseEventTrackerFactory(
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTrackerFactory {

    override fun create(event: Event): Resolution<FirebaseEventTracker> = when (event) {
        is ScreenViewEvent -> Resolution.Claimed(ScreenViewEventTracker(event, firebaseAnalytics))
        else -> Resolution.Declined
    }
}
