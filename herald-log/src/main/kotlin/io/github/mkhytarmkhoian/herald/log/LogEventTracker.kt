package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.asString

/**
 * One pending log line on behalf of one event.
 *
 * A `fun interface`, so a one-off handler inside a custom factory can be a lambda rather than a
 * class.
 */
public fun interface LogEventTracker {
    public suspend fun track()
}

/** Prefix on every line Herald prints, so a log filter can isolate them. */
internal const val LOG_TAG = "[herald]"

/** Widest record kind (`enabled`), so headlines line up whatever the record is. */
private const val KIND_WIDTH = 7

/**
 * Renders one record as one string: a header line, then a branch per parameter with the last one
 * closed by `└─`, values aligned on the longest key.
 *
 * One string, and one log call, so records from concurrent providers cannot interleave.
 *
 * A record with no headline — `reset`, `start`, `flush` — is the bare kind, with no padding to
 * trail the line.
 */
internal fun logRecord(kind: String, headline: String, parameters: Map<String, AnalyticsValue>) =
    buildString {
        append(LOG_TAG).append(' ')
        if (headline.isEmpty()) append(kind) else append(kind.padEnd(KIND_WIDTH)).append(' ').append(headline)

        if (parameters.isEmpty()) return@buildString

        val keys = parameters.keys.sorted()
        val keyWidth = keys.maxOf { it.length }
        keys.forEachIndexed { index, key ->
            val branch = if (index == keys.lastIndex) "└─" else "├─"
            append("\n    ").append(branch).append(' ')
                .append(key.padEnd(keyWidth))
                .append(" = ")
                .append(parameters.getValue(key).asString)
        }
    }
