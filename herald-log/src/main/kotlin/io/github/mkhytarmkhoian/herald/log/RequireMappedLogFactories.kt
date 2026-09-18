package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException

/**
 * Fails on any event that reaches it. Put it last in a chain to turn an event nobody wired up into
 * a loud failure rather than a silent no-op.
 *
 * The alternative to ending a chain with [GenericLogEventTrackerFactory], which sends everything.
 */
public object RequireMappedLogEventTrackerFactory : LogEventTrackerFactory, FallbackFactory {
    override fun create(event: Event): Resolution<LogEventTracker> = throw UnhandledEventException(event)
}

/** The [RequireMappedLogEventTrackerFactory] counterpart for properties. */
public object RequireMappedLogPropertySetterFactory : LogPropertySetterFactory, FallbackFactory {
    override fun create(property: Property): Resolution<LogPropertySetter> =
        throw UnhandledPropertyException(property)
}
