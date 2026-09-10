package io.github.mkhytarmkhoian.herald

/**
 * A durable attribute attached to later events rather than to one of them.
 *
 * The value keeps its type, so a number stays a number for vendors that support numeric
 * properties and can be used in numeric filters. Vendors whose properties are string-only
 * flatten it with [asString].
 */
public interface Property {
    public val name: String
    public val value: AnalyticsValue
}
