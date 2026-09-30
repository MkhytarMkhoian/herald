package samples.guides

import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.asString

/** The wrapper most apps already have, called from everywhere. */
class LegacyAnalytics {
    fun logEvent(name: String, params: Map<String, String>) {}
}

fun migrating(legacy: LegacyAnalytics) {
    // --8<-- [start:bridge]
    // Step 1: Herald forwards to the old wrapper, so migrated and unmigrated code report the same.
    val herald = Herald {
        provider(
            name = "legacy",
            events = EventTrackerService { event ->
                legacy.logEvent(event.name, event.parameters.mapValues { (_, value) -> value.asString })
            },
        )
    }
    // --8<-- [end:bridge]
    herald.hashCode()
}
