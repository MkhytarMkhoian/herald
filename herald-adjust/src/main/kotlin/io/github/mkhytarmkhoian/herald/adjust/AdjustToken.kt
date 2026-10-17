package io.github.mkhytarmkhoian.herald.adjust

import io.github.mkhytarmkhoian.herald.Event
import kotlin.reflect.KClass

/**
 * The Adjust dashboard token for events of type [E], or of a subclass of it:
 * `AdjustToken<SignUpCompleted>("abc123")`.
 */
public class AdjustToken<E : Event>(
    internal val eventClass: KClass<E>,
    internal val token: String,
) {
    internal fun matches(event: Event): Boolean = eventClass.isInstance(event)

    override fun toString(): String = "AdjustToken<${eventClass.simpleName}>($token)"
}

/** The Adjust dashboard token for events of type [E]: `AdjustToken<SignUpCompleted>("abc123")`. */
public inline fun <reified E : Event> AdjustToken(token: String): AdjustToken<E> =
    AdjustToken(E::class, token)
