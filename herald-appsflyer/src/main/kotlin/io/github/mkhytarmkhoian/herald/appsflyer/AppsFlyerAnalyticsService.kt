package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity

/**
 * AppsFlyer's lifecycle and identity, over an [AppsFlyerLib] the consumer has initialised.
 *
 * Call `init` yourself, in `Application.onCreate` — the dev key, conversion listener, deep-link
 * listener and everything else AppsFlyer offers are set there. AppsFlyer 7 also skips `start()`
 * unless a `SessionReadyListener` was registered first:
 *
 * ```kotlin
 * val appsFlyer = AppsFlyerLib.getInstance()
 *     .init(devKey, conversionListener, application)
 * appsFlyer.registerSessionReadyListener { /* ... */ }
 * AppsFlyerAnalyticsService(appsFlyer, application)
 * ```
 *
 * **AppsFlyer starts on consent, not on [start].** [start] only stops the SDK, so a fresh install
 * sends nothing — no install, no launch, no events. [setEnabled] with `true` resumes it and calls
 * `start()`, in the order AppsFlyer requires after a stop: `stop(false)`, then `start()`. Do not
 * call `start()` yourself. AppsFlyer does not persist the stopped state, and [start] applies it
 * every launch, so re-apply the stored consent decision after start-up.
 *
 * AppsFlyer 7 does not persist the customer user id either: call [identify] on every cold start,
 * not only at sign-in, and before consent is re-applied so the launch carries it.
 *
 * Granular consent — DMA via `setConsentData`, `anonymizeUser` — is set on the [AppsFlyerLib] you
 * own.
 */
public class AppsFlyerAnalyticsService(
    private val appsFlyer: AppsFlyerLib,
    private val context: Context,
) : AnalyticsLifecycleService, IdentifiableUserService, ConsentService {

    override suspend fun start() {
        appsFlyer.stop(true, context)
    }

    override suspend fun setEnabled(enabled: Boolean) {
        appsFlyer.stop(!enabled, context)
        if (enabled) appsFlyer.start()
    }

    override suspend fun identify(identity: Identity) {
        appsFlyer.setCustomerUserId(identity.userId)
    }

    override suspend fun reset() {
        appsFlyer.setCustomerUserId(null)
    }
}
