package io.github.mkhytarmkhoian.herald

/**
 * Thrown when an event reaches the end of a factory chain without any factory claiming it.
 *
 * Only a strict factory throws this. A chain that does not end in one ignores unclaimed events.
 *
 * The message names the event and its class. With R8 minification on, the class name is
 * obfuscated (retrace it with the mapping file); the event name is a plain string and reads as-is.
 */
public class UnhandledEventException(public val event: Event) : IllegalStateException(
    "No factory claimed event '${event.name}' (${event.javaClass.name}). " +
        "Add a factory for it, or end the chain with a generic factory to send it as-is."
)
