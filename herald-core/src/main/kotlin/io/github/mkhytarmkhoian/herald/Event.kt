package io.github.mkhytarmkhoian.herald

/**
 * Something that happened, in the app's own vocabulary.
 *
 * [name] identifies the event and [parameters] carries its payload.
 */
public interface Event {
    public val name: String

    /** Empty by default. Build it with [parameters]. */
    public val parameters: Map<String, AnalyticsValue> get() = emptyMap()
}
