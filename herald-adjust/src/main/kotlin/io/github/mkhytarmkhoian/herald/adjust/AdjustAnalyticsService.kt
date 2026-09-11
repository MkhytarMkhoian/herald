package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustInstance

private const val DEFAULT_IDENTITY_PARAMETER = "user_id"

/**
 * Adjust's lifecycle and identity, over an [AdjustConfig] the consumer builds.
 *
 * The config arrives ready-made, so configure Adjust at your composition root — data residency,
 * log level, default tracker, attribution callbacks and anything else it offers.
 *
 * ```kotlin
 * val config = AdjustConfig(application, appToken, AdjustConfig.ENVIRONMENT_PRODUCTION).apply {
 *     urlStrategy = AdjustConfig.DATA_RESIDENCY_EU
 *     setLogLevel(LogLevel.SUPPRESS)
 * }
 * AdjustAnalyticsService(adjust, config)
 * ```
 *
 * [start] calls [AdjustInstance.initSdk]; do not call it yourself. It does not register activity
 * lifecycle callbacks either — Adjust v5 registers its own at process start.
 *
 * **This adapter opts out for you.** [start] disables Adjust immediately after init, so a fresh
 * install collects nothing until [setEnabled] is called with `true`. Adjust persists that flag and
 * [start] overrides it every launch, so re-apply the stored consent decision after start-up.
 *
 * @param identityParameter the Adjust global callback parameter [identify] writes the user id to
 * and [reset] removes. It must match a callback parameter configured in your Adjust dashboard.
 * Defaults to Adjust's customary `user_id`. Properties go to the same bag through
 * [GenericAdjustPropertySetterFactory], so no [io.github.mkhytarmkhoian.herald.Property] may use
 * this name: it would overwrite the identity, and [reset] would then remove the property.
 */
public class AdjustAnalyticsService(
    private val adjust: AdjustInstance,
    private val config: AdjustConfig,
    private val identityParameter: String = DEFAULT_IDENTITY_PARAMETER,
) : AnalyticsLifecycleService, IdentifiableUserService, ConsentService {

    override suspend fun identify(identity: Identity) {
        adjust.addGlobalCallbackParameter(identityParameter, identity.userId)
    }

    override suspend fun reset() {
        adjust.removeGlobalCallbackParameter(identityParameter)
    }

    override suspend fun start() {
        adjust.initSdk(config)
        adjust.disable() // Collect nothing until setEnabled(true); see the note on the class.
    }

    override suspend fun setEnabled(enabled: Boolean) {
        if (enabled) adjust.enable() else adjust.disable()
    }
}
