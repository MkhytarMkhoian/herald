package io.github.mkhytarmkhoian.herald.firebase

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
 * The alternative to ending a chain with [GenericFirebaseEventTrackerFactory], which sends everything.
 */
public object RequireMappedFirebaseEventTrackerFactory : FirebaseEventTrackerFactory, FallbackFactory {
    override fun create(event: Event): Resolution<FirebaseEventTracker> = throw UnhandledEventException(event)
}

/** The [RequireMappedFirebaseEventTrackerFactory] counterpart for properties. */
public object RequireMappedFirebasePropertySetterFactory : FirebasePropertySetterFactory, FallbackFactory {
    override fun create(property: Property): Resolution<FirebasePropertySetter> =
        throw UnhandledPropertyException(property)
}
