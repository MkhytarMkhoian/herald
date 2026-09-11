package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.parameters
import org.junit.Test
import kotlin.test.assertEquals

class LogRecordTest {

    @Test
    fun `A record without parameters is a single line`() {
        val record = logRecord(kind = "screen", headline = "CheckoutScreen", parameters = emptyMap())

        assertEquals("[herald] screen  CheckoutScreen", record)
    }

    @Test
    fun `Parameters are branches, aligned on the longest key, with the last one closed`() {
        val record = logRecord(
            kind = "event",
            headline = "checkout_started",
            parameters = parameters {
                put("plan", "pro")
                put("seats", 3)
                put("price", 9.99)
                put("trial", false)
            },
        )

        assertEquals(
            """
            [herald] event   checkout_started
                ├─ plan  = pro
                ├─ price = 9.99
                ├─ seats = 3
                └─ trial = false
            """.trimIndent(),
            record,
        )
    }

    @Test
    fun `Keys are sorted, so the same event always prints the same way`() {
        val record = logRecord(
            kind = "event",
            headline = "e",
            parameters = parameters {
                put("zulu", "z")
                put("alpha", "a")
            },
        )

        assertEquals(
            """
            [herald] event   e
                ├─ alpha = a
                └─ zulu  = z
            """.trimIndent(),
            record,
        )
    }

    @Test
    fun `A single parameter is closed rather than branched`() {
        val record = logRecord(
            kind = "event",
            headline = "e",
            parameters = parameters { put("only", 1) },
        )

        assertEquals(
            """
            [herald] event   e
                └─ only = 1
            """.trimIndent(),
            record,
        )
    }

    @Test
    fun `A record with no headline is the bare kind, with nothing trailing`() {
        assertEquals("[herald] reset", logRecord(kind = "reset", headline = "", parameters = emptyMap()))
    }

    @Test
    fun `Record kinds are padded so headlines line up`() {
        val event = logRecord(kind = "event", headline = "x", parameters = emptyMap())
        val screen = logRecord(kind = "screen", headline = "x", parameters = emptyMap())
        val property = logRecord(kind = "prop", headline = "x", parameters = emptyMap())
        val enabled = logRecord(kind = "enabled", headline = "x", parameters = emptyMap())

        assertEquals(event.indexOf('x'), screen.indexOf('x'))
        assertEquals(event.indexOf('x'), property.indexOf('x'))
        assertEquals(event.indexOf('x'), enabled.indexOf('x'))
    }
}
