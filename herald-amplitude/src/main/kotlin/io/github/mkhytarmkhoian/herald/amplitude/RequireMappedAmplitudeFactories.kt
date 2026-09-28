package io.github.mkhytarmkhoian.herald.amplitude

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
 * The alternative to ending a chain with [GenericAmplitudeEventTrackerFactory], which sends everything.
 */
public object RequireMappedAmplitudeEventTrackerFactory : AmplitudeEventTrackerFactory, FallbackFactory {
    override fun create(event: Event): Resolution<AmplitudeEventTracker> = throw UnhandledEventException(event)
}

/**
 * The [RequireMappedAmplitudeEventTrackerFactory] counterpart for properties, and the alternative to
 * ending a chain with [GenericAmplitudePropertySetterFactory].
 */
public object RequireMappedAmplitudePropertySetterFactory : AmplitudePropertySetterFactory, FallbackFactory {
    override fun create(property: Property): Resolution<AmplitudePropertySetter> =
        throw UnhandledPropertyException(property)
}
