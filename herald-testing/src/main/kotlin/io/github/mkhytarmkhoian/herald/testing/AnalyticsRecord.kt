package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.Identity
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.asString

/**
 * One call a [FakeAnalyticsProvider] received.
 *
 * Every capability lands in the same list, so a test can ask about ordering between them — whether
 * a property was set before the event meant to carry it, or whether `reset()` came after the last
 * event of a session.
 */
public sealed interface AnalyticsRecord {
    public data class Tracked(val event: Event) : AnalyticsRecord
    public data class PropertySet(val property: Property) : AnalyticsRecord
    public data class Identified(val identity: Identity) : AnalyticsRecord
    public data class EnabledSet(val enabled: Boolean) : AnalyticsRecord
    public data object Reset : AnalyticsRecord
    public data object Started : AnalyticsRecord
    public data object Flushed : AnalyticsRecord
}

/** How a record appears inside an assertion failure. */
internal fun AnalyticsRecord.describe(): String = when (this) {
    is AnalyticsRecord.Tracked -> "event    ${event.name}${event.parameters.describe()}"
    is AnalyticsRecord.PropertySet -> "property ${property.name} = ${property.value.asString}"
    is AnalyticsRecord.Identified -> "identify ${identity.userId}"
    is AnalyticsRecord.EnabledSet -> "enabled  $enabled"
    AnalyticsRecord.Reset -> "reset"
    AnalyticsRecord.Started -> "start"
    AnalyticsRecord.Flushed -> "flush"
}

internal fun Map<String, AnalyticsValue>.describe(): String =
    if (isEmpty()) "" else entries.sortedBy { it.key }
        .joinToString(prefix = " { ", postfix = " }") { "${it.key} = ${it.value.asString}" }
