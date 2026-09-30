package io.github.mkhytarmkhoian.herald

/**
 * The master switch for whether a vendor may collect at all.
 *
 * One boolean, because that is the only consent signal every vendor implements.
 *
 * This does not cover collection *before* the first call. The Adjust and AppsFlyer adapters start
 * silent. Firebase needs `firebase_analytics_collection_enabled=false` in the manifest, Mixpanel
 * needs `MixpanelOptions.optOutTrackingDefault(true)` and Amplitude `Configuration(optOut = true)`
 * at construction, all outside Herald.
 */
public interface ConsentService {

    /** Turn collection on or off. */
    public suspend fun setEnabled(enabled: Boolean)
}
