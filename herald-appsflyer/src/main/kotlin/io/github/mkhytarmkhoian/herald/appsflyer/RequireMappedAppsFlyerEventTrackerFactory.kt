package io.github.mkhytarmkhoian.herald.appsflyer

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.UnhandledEventException

/**
 * Fails on any event that reaches it. Put it last in a chain to turn an event nobody wired up into
 * a loud failure rather than a silent no-op.
 *
 * The alternative to ending a chain with [GenericAppsFlyerEventTrackerFactory], which sends everything.
 */
public object RequireMappedAppsFlyerEventTrackerFactory : AppsFlyerEventTrackerFactory, FallbackFactory {
    override fun create(event: Event): Resolution<AppsFlyerEventTracker> = throw UnhandledEventException(event)
}
