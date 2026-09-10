package io.github.mkhytarmkhoian.herald

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ResolutionTest {

    private val handler = "a-handler"

    @Test
    fun `Claimed should carry its handlers`() {
        assertEquals(listOf(handler), Resolution.Claimed(handler).handlers)
    }

    // There must be exactly one way to say "send nothing", and that is Dropped.
    @Test
    fun `Claimed should reject an empty list, since that is what Dropped is for`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            Resolution.Claimed(emptyList<String>())
        }

        assertTrue(
            "Dropped" in failure.message.orEmpty(),
            "The message should point at Dropped, but was: ${failure.message}",
        )
    }

    // Claimed() does not compile, so the vararg path cannot produce an empty Claimed at all.
    @Test
    fun `The vararg constructor should agree with the list one`() {
        assertEquals(Resolution.Claimed(handler), Resolution.Claimed(handler))
        assertEquals(Resolution.Claimed("a", "b"), Resolution.Claimed("a", "b"))
    }

    @Test
    fun `Dropped and Declined should both carry no handlers`() {
        assertEquals(emptyList(), Resolution.Dropped.handlers)
        assertEquals(emptyList(), Resolution.Declined.handlers)
    }

    // Dropped and Declined both carry no handlers, so they must stay distinguishable by identity.
    @Test
    fun `Dropped and Declined should not be equal to each other`() {
        assertTrue(Resolution.Dropped != Resolution.Declined)
    }
}
