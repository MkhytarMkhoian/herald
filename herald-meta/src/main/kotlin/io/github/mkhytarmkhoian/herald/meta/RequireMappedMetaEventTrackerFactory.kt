package io.github.mkhytarmkhoian.herald.meta

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.UnhandledEventException

/** Fails on any event that reaches it, so the error reporter shows events nobody mapped. */
public object RequireMappedMetaEventTrackerFactory : MetaEventTrackerFactory, FallbackFactory {
    override fun create(event: Event): Resolution<MetaEventTracker> = throw UnhandledEventException(event)
}
