package io.github.mkhytarmkhoian.herald

/** The [UnhandledEventException] counterpart for properties. */
public class UnhandledPropertyException(public val property: Property) : IllegalStateException(
    "No factory claimed property '${property.name}' (${property::class.qualifiedName}). " +
        "Add a factory for it, or end the chain with a generic factory to set it as-is."
)
