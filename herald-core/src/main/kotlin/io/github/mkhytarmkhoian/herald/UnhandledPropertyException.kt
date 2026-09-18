package io.github.mkhytarmkhoian.herald

/**
 * The [UnhandledEventException] counterpart for properties. As there, the class name in the
 * message is obfuscated under R8 minification; the property name reads as-is.
 */
public class UnhandledPropertyException(public val property: Property) : IllegalStateException(
    "No factory claimed property '${property.name}' (${property.javaClass.name}). " +
        "Add a factory for it, or end the chain with a generic factory to set it as-is."
)
