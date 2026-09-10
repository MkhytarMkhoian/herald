package io.github.mkhytarmkhoian.herald

/**
 * Which call into a vendor adapter was in flight when it failed, and what it was working on.
 *
 * Carries names only, never values: an event's name but not its parameters, a property's name but
 * not its value, and nothing at all for [Identify]. That keeps personal data out of whatever a
 * consumer's [AnalyticsErrorReporter] forwards to.
 */
public sealed interface AnalyticsOperation {

    /** Tracking the event called [eventName]. */
    public data class Track(val eventName: String) : AnalyticsOperation

    /** Setting the property called [propertyName]. */
    public data class SetProperty(val propertyName: String) : AnalyticsOperation

    /** Turning collection on or off. */
    public data class SetEnabled(val enabled: Boolean) : AnalyticsOperation

    /** Identifying the current user. */
    public data object Identify : AnalyticsOperation

    /** Clearing the current identity. */
    public data object Reset : AnalyticsOperation

    /** Starting the vendor SDK. */
    public data object Start : AnalyticsOperation

    /** Asking the vendor to deliver anything buffered. */
    public data object Flush : AnalyticsOperation
}
