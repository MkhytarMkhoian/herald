package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException
import io.github.mkhytarmkhoian.herald.Resolution

/**
 * Fails on any event that reaches it. Put it last in a chain to turn an event nobody wired up into
 * a loud failure rather than a silent no-op.
 *
 * Rarely what you want for Adjust: an event reaches it only with a token, so most events are
 * expected to go unclaimed and this would fail on all of them.
 */
public object RequireMappedAdjustEventTrackerFactory : AdjustEventTrackerFactory {
    override fun create(event: Event): Resolution<AdjustEventTracker> = throw UnhandledEventException(event)
}

/**
 * The [RequireMappedAdjustEventTrackerFactory] counterpart for properties, and the alternative to
 * ending a chain with [GenericAdjustPropertySetterFactory].
 *
 * Unlike events, this is a real choice: every property can reach Adjust, so failing on an
 * unclaimed one is a reasonable policy.
 */
public object RequireMappedAdjustPropertySetterFactory : AdjustPropertySetterFactory {
    override fun create(property: Property): Resolution<AdjustPropertySetter> =
        throw UnhandledPropertyException(property)
}
