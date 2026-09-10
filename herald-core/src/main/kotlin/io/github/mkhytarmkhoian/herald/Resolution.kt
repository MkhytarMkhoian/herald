package io.github.mkhytarmkhoian.herald

/**
 * What one factory decided about one event or property.
 *
 * A factory answers for what it knows and passes the rest along:
 *
 * - [Claimed] — mine, and here is what to send.
 * - [Dropped] — mine, and it goes nowhere. The chain stops, so no later factory sees it.
 * - [Declined] — not mine. The next factory gets a look.
 */
public sealed interface Resolution<out T> {

    /** The handlers to run. Empty for [Dropped] and [Declined]. */
    public val handlers: List<T>

    /** Mine, and these are the handlers to run. Must not be empty — use [Dropped] for that. */
    public data class Claimed<T>(override val handlers: List<T>) : Resolution<T> {

        /** For handlers written out directly: `Claimed(ScreenViewEventTracker(event, analytics))`. */
        public constructor(vararg handlers: T) : this(handlers.toList())

        init {
            require(handlers.isNotEmpty()) {
                "Claimed needs at least one handler. Use Dropped to claim something and send nothing."
            }
        }
    }

    /** Mine, and it goes nowhere. Stops the chain, so no later factory can claim it. */
    public data object Dropped : Resolution<Nothing> {
        override val handlers: List<Nothing> get() = emptyList()
    }

    /** Not mine — try the next factory in the chain. */
    public data object Declined : Resolution<Nothing> {
        override val handlers: List<Nothing> get() = emptyList()
    }
}
