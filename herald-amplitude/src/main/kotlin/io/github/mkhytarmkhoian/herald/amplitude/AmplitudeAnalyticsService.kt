package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity

/**
 * Amplitude's lifecycle and identity, over an [Amplitude] the consumer owns.
 *
 * Amplitude starts itself when it is constructed, from a `Configuration` you build — API key,
 * server zone, autocapture and everything else it offers are set there. [start] only waits for
 * that build to finish.
 *
 * **Before consent arrives, Amplitude collects.** Its default is opted *in*. Build it opted out
 * instead, and [setEnabled] opts in:
 *
 * ```kotlin
 * val amplitude = Amplitude(Configuration(apiKey, context, optOut = true))
 * ```
 *
 * Amplitude does not persist the opt-out: every launch starts from the `Configuration`, so
 * re-apply the stored consent decision after start-up.
 */
public class AmplitudeAnalyticsService(
    private val amplitude: Amplitude,
) : AnalyticsLifecycleService, IdentifiableUserService, ConsentService {

    override suspend fun start() {
        amplitude.isBuilt.await()
    }

    override suspend fun flush() {
        amplitude.flush()
    }

    override suspend fun setEnabled(enabled: Boolean) {
        amplitude.optOut = !enabled
    }

    override suspend fun identify(identity: Identity) {
        amplitude.setUserId(identity.userId)
    }

    override suspend fun reset() {
        amplitude.reset()
    }
}
