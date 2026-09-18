package io.github.mkhytarmkhoian.herald.mixpanel

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
 * The alternative to ending a chain with [GenericMixpanelEventTrackerFactory], which sends everything.
 */
public object RequireMappedMixpanelEventTrackerFactory : MixpanelEventTrackerFactory, FallbackFactory {
    override fun create(event: Event): Resolution<MixpanelEventTracker> = throw UnhandledEventException(event)
}

/**
 * The [RequireMappedMixpanelEventTrackerFactory] counterpart for properties, and the alternative to
 * ending a chain with [GenericMixpanelPropertySetterFactory].
 */
public object RequireMappedMixpanelPropertySetterFactory : MixpanelPropertySetterFactory, FallbackFactory {
    override fun create(property: Property): Resolution<MixpanelPropertySetter> =
        throw UnhandledPropertyException(property)
}
