package io.github.mkhytarmkhoian.herald

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FallbackFactoryTest {

    private object Partial { override fun toString() = "Partial" }
    private object Fallback : FallbackFactory { override fun toString() = "Fallback" }

    @Test
    fun `a chain with the fallback last is accepted`() {
        requireFallbackLast(listOf(Partial, Partial, Fallback))
    }

    @Test
    fun `a chain with no fallback is accepted`() {
        requireFallbackLast(listOf(Partial, Partial))
        requireFallbackLast(emptyList())
    }

    @Test
    fun `a fallback in the middle is refused, naming it and where it is`() {
        val e = assertFailsWith<IllegalArgumentException> { requireFallbackLast(listOf(Partial, Fallback, Partial)) }

        assertEquals(
            "Fallback answers for everything, so nothing placed after it is ever asked. " +
                "It is at position 1 of 3; move it to the end.",
            e.message,
        )
    }

    @Test
    fun `two fallbacks are refused`() {
        val e = assertFailsWith<IllegalArgumentException> { requireFallbackLast(listOf(Partial, Fallback, Fallback)) }

        assertEquals(
            "A chain can end in one fallback factory; this one has 2: Fallback at position 1, Fallback at position 2.",
            e.message,
        )
    }
}
