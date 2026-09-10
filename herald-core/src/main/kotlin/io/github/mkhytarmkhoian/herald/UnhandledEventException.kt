package io.github.mkhytarmkhoian.herald

/**
 * Thrown when an event reaches the end of a factory chain without any factory claiming it.
 *
 * Only a strict factory throws this. A chain that does not end in one ignores unclaimed events.
 */
public class UnhandledEventException(public val event: Event) : IllegalStateException(
    "No factory claimed event '${event.name}' (${event::class.qualifiedName}). " +
        "Add a factory for it, or end the chain with a generic factory to send it as-is."
)
