package io.github.mkhytarmkhoian.herald

/**
 * A factory that answers for every event or property: the generic ones, which send as-is, and
 * the `RequireMapped` ones, which throw. It belongs at the end of a chain, because nothing
 * placed after it is ever asked. Every composite factory checks this when it is built.
 */
public interface FallbackFactory

/**
 * Fails if [factories] has more than one [FallbackFactory], or one that is not last. Called by
 * the composite factories when they are built, so a wrong chain fails at start-up with the
 * factory named, not at the first event with nothing sent.
 */
public fun requireFallbackLast(factories: List<Any>) {
    val fallbacks = factories.withIndex().filter { (_, factory) -> factory is FallbackFactory }
    require(fallbacks.size <= 1) {
        "A chain can end in one fallback factory; this one has ${fallbacks.size}: " +
            fallbacks.joinToString { (index, factory) -> "$factory at position $index" } + "."
    }
    val fallback = fallbacks.singleOrNull() ?: return
    require(fallback.index == factories.lastIndex) {
        "${fallback.value} answers for everything, so nothing placed after it is ever asked. " +
            "It is at position ${fallback.index} of ${factories.size}; move it to the end."
    }
}
