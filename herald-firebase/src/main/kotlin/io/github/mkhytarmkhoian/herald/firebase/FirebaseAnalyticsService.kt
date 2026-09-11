package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase's lifecycle and identity, over a [FirebaseAnalytics] the consumer owns.
 *
 * **Before consent arrives, Firebase collects.** [start] does not opt out, because Firebase's
 * off-by-default switch is a manifest flag read at initialisation, before any Herald code runs:
 *
 * ```xml
 * <meta-data android:name="firebase_analytics_collection_enabled" android:value="false" />
 * ```
 *
 * Set that flag if you need a fresh install to collect nothing until the user has answered.
 * [setEnabled] then flips collection on, and Firebase persists the choice across launches.
 */
public class FirebaseAnalyticsService(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val identificationEnabled: Boolean
) : AnalyticsLifecycleService, IdentifiableUserService, ConsentService {

    override suspend fun start() {
        // Firebase Analytics initialises itself from google-services.json.
    }

    override suspend fun setEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    override suspend fun identify(identity: Identity) {
        if (identificationEnabled) {
            firebaseAnalytics.setUserId(identity.userId)
        }
    }

    override suspend fun reset() {
        if (identificationEnabled) {
            firebaseAnalytics.setUserId(null)
        }
    }
}
