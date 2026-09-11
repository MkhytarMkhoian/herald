package io.github.mkhytarmkhoian.herald.testing

import io.github.mkhytarmkhoian.herald.AnalyticsValue
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.asString

/**
 * Assertions about one tracked event, handed to the block of
 * [FakeAnalyticsProvider.assertTracked].
 *
 * The [param] overloads mirror the `parameters { }` builder, so you assert a value the same way
 * the event declared it. `param("seats", 3)` expects an [AnalyticsValue.Int] and fails against an
 * [AnalyticsValue.String] holding `"3"` — the two reach a vendor differently.
 */
public class TrackedEventAssert internal constructor(
    private val name: String,
    private val event: Event,
    private val fail: (String) -> Nothing,
) {

    public fun param(key: String, value: String): Unit = param(key, AnalyticsValue.String(value))

    public fun param(key: String, value: Int): Unit = param(key, AnalyticsValue.Int(value))

    public fun param(key: String, value: Long): Unit = param(key, AnalyticsValue.Long(value))

    public fun param(key: String, value: Float): Unit = param(key, AnalyticsValue.Float(value))

    public fun param(key: String, value: Double): Unit = param(key, AnalyticsValue.Double(value))

    public fun param(key: String, value: Boolean): Unit = param(key, AnalyticsValue.Boolean(value))

    public fun param(key: String, value: AnalyticsValue) {
        val actual = event.parameters[key]
            ?: fail("Event '$name' has no parameter '$key'${event.parameters.describe()}.")
        if (actual != value) {
            fail(
                "Event '$name' parameter '$key' was ${actual.describeTyped()}, " +
                    "expected ${value.describeTyped()}."
            )
        }
    }

    /** Asserts the event carries no parameters at all. */
    public fun noParameters() {
        if (event.parameters.isNotEmpty()) {
            fail("Expected '$name' to carry no parameters, but it carried${event.parameters.describe()}.")
        }
    }
}

/** `Int(3)` rather than `3`, so a type mismatch reads as one. */
internal fun AnalyticsValue.describeTyped(): String =
    "${this::class.simpleName}(${asString})"
