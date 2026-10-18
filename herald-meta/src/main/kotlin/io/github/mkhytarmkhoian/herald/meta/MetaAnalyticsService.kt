package io.github.mkhytarmkhoian.herald.meta

import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity

/**
 * Meta's start, sign-in and consent, over an [AppEventsLogger] the app creates.
 *
 * [setEnabled] turns Meta's own collection on or off: the events it logs by itself, such as
 * installs, and the advertiser id. It doesn't stop the events your app logs.
 *
 * Meta's SDK starts before any Herald code runs, so a fresh install collects nothing by itself only
 * if `AndroidManifest.xml` turns its automatic collection off:
 *
 * ```xml
 * <meta-data android:name="com.facebook.sdk.AutoInitEnabled" android:value="false" />
 * <meta-data android:name="com.facebook.sdk.AutoLogAppEventsEnabled" android:value="false" />
 * <meta-data android:name="com.facebook.sdk.AdvertiserIDCollectionEnabled" android:value="false" />
 * ```
 *
 * [setEnabled] then turns them on or off. Call it with the stored answer on every launch.
 */
public class MetaAnalyticsService(
    private val logger: AppEventsLogger,
) : AnalyticsLifecycleService, IdentifiableUserService, ConsentService {

    /** Meta's SDK starts itself, so there is nothing to start. */
    override suspend fun start() {}

    override suspend fun flush() {
        logger.flush()
    }

    override suspend fun setEnabled(enabled: Boolean) {
        FacebookSdk.setAutoLogAppEventsEnabled(enabled)
        FacebookSdk.setAdvertiserIDCollectionEnabled(enabled)
        FacebookSdk.setAutoInitEnabled(enabled)
        if (enabled) FacebookSdk.fullyInitialize()
    }

    override suspend fun identify(identity: Identity) {
        AppEventsLogger.setUserID(identity.userId)
    }

    override suspend fun reset() {
        AppEventsLogger.clearUserID()
    }
}
