package io.github.mkhytarmkhoian.herald.firebase

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.UnhandledPropertyException
import io.github.mkhytarmkhoian.herald.Resolution

/**
 * Fails on any event that reaches it. Put it last in a chain to turn an event nobody wired up into
 * a loud failure rather than a silent no-op.
 *
 * The alternative to ending a chain with [GenericFirebaseEventTrackerFactory], which sends everything.
 */
public object RequireMappedFirebaseEventTrackerFactory : FirebaseEventTrackerFactory {
    override fun create(event: Event): Resolution<FirebaseEventTracker> = throw UnhandledEventException(event)
}

/** The [RequireMappedFirebaseEventTrackerFactory] counterpart for properties. */
public object RequireMappedFirebasePropertySetterFactory : FirebasePropertySetterFactory {
    override fun create(property: Property): Resolution<FirebasePropertySetter> =
        throw UnhandledPropertyException(property)
}
