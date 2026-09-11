package io.github.mkhytarmkhoian.herald.firebase.trackers

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.firebase.FirebaseEventTracker
import io.github.mkhytarmkhoian.herald.firebase.toBundle

public class ScreenViewEventTracker(
    private val event: ScreenViewEvent,
    private val firebaseAnalytics: FirebaseAnalytics,
) : FirebaseEventTracker {

    override suspend fun track() {
        val bundle = event.parameters.toBundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, event.screenName)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
