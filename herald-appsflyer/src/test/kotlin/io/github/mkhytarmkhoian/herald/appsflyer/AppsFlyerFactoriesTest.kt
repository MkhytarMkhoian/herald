package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.UnhandledEventException
import io.github.mkhytarmkhoian.herald.appsflyer.trackers.GenericEventTracker
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class AppsFlyerFactoriesTest {

    private val appsFlyer: AppsFlyerLib = mockk()
    private val context: Context = mockk()

    @Test
    fun `Generic factory should claim any event`() {
        val factory = GenericAppsFlyerEventTrackerFactory(appsFlyer, context)

        assertIs<GenericEventTracker>(factory.create(mockk<Event>()).handlers.single())
    }

    @Test
    fun `RequireMapped factory should throw, naming the event`() {
        val event: Event = mockk { every { name } returns "unmapped" }

        val failure = assertFailsWith<UnhandledEventException> {
            RequireMappedAppsFlyerEventTrackerFactory.create(event)
        }

        assertSame(event, failure.event)
    }
}
