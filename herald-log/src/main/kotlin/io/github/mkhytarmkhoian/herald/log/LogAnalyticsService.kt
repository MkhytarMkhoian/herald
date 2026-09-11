package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity

/**
 * Prints the calls that are not events or properties, so the log shows the whole story: whether
 * consent was ever granted, whether `identify` ran before the first event, whether `reset` fired
 * on sign-out.
 *
 * The user id is printed as-is. That is the point in a debug build and a leak in a release one,
 * so register this provider under the same condition as the rest of the log adapter.
 *
 * ```kotlin
 * val log = LogAnalyticsService(logger)
 * provider(name = "log", events = logTracker, properties = logTracker,
 *          identity = log, lifecycle = log, consent = log)
 * ```
 */
public class LogAnalyticsService(
    private val logger: AnalyticsLogger,
) : IdentifiableUserService, AnalyticsLifecycleService, ConsentService {

    override suspend fun identify(identity: Identity) {
        logger.log(logRecord(kind = "user", headline = identity.userId, parameters = emptyMap()))
    }

    override suspend fun reset() {
        logger.log(logRecord(kind = "reset", headline = "", parameters = emptyMap()))
    }

    override suspend fun start() {
        logger.log(logRecord(kind = "start", headline = "", parameters = emptyMap()))
    }

    override suspend fun flush() {
        logger.log(logRecord(kind = "flush", headline = "", parameters = emptyMap()))
    }

    override suspend fun setEnabled(enabled: Boolean) {
        logger.log(logRecord(kind = "enabled", headline = enabled.toString(), parameters = emptyMap()))
    }
}
