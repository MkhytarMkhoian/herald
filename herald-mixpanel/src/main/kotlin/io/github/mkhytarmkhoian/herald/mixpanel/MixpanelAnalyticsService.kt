package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity

/**
 * Mixpanel's lifecycle and identity, over a [MixpanelAPI] the consumer owns.
 *
 * Data residency is set on the [MixpanelAPI] before you hand it over — `setServerURL` is a method
 * on the instance, so this adapter leaves it alone.
 *
 * **Before consent arrives, Mixpanel collects.** Its own default is opted *in*, and [start] does
 * not opt out: `optOutTracking()` deletes unflushed events and clears the stored identity, so
 * calling it every launch would discard the previous session and de-identify a consenting user.
 * Use the construction-time switch instead:
 *
 * ```kotlin
 * val options = MixpanelOptions.Builder().optOutTrackingDefault(true).build()
 * val mixpanel = MixpanelAPI.getInstance(context, token, trackAutomaticEvents, options)
 * ```
 *
 * [setEnabled] then opts in, and the state persists across launches.
 */
public class MixpanelAnalyticsService(
    private val mixpanel: MixpanelAPI,
    private val loggingEnabled: Boolean,
    private val identificationEnabled: Boolean
) : AnalyticsLifecycleService, IdentifiableUserService, ConsentService {

    override suspend fun start() {
        mixpanel.setEnableLogging(loggingEnabled)
    }

    override suspend fun flush() {
        mixpanel.flush()
    }

    override suspend fun setEnabled(enabled: Boolean) {
        if (enabled.not()) {
            mixpanel.flush()
            mixpanel.optOutTracking()
        } else {
            mixpanel.optInTracking()
        }
    }

    override suspend fun identify(identity: Identity) {
        if (identificationEnabled) {
            mixpanel.identify(identity.userId, true)
        }
    }

    override suspend fun reset() {
        if (identificationEnabled) {
            mixpanel.reset()
        }
    }
}
