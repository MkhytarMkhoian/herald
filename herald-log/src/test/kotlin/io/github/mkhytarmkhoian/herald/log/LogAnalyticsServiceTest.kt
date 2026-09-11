package io.github.mkhytarmkhoian.herald.log

import io.github.mkhytarmkhoian.herald.Identity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class LogAnalyticsServiceTest {

    private val logger = RecordingLogger()
    private val service = LogAnalyticsService(logger)

    @Test
    fun `Every lifecycle, identity and consent call prints one line, in order`() = runTest {
        service.start()
        service.setEnabled(true)
        service.identify(Identity("user-1"))
        service.flush()
        service.reset()
        service.setEnabled(false)

        assertEquals(
            listOf(
                "[herald] start",
                "[herald] enabled true",
                "[herald] user    user-1",
                "[herald] flush",
                "[herald] reset",
                "[herald] enabled false",
            ),
            logger.messages,
        )
    }
}
