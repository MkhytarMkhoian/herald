package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AmplitudeAnalyticsServiceTest {

    private companion object {
        const val USER_ID = "test-user-id"
    }

    private val amplitude: Amplitude = mockk(relaxed = true)

    private val service = AmplitudeAnalyticsService(amplitude)

    @Test
    fun `On start should wait for amplitude to finish building`() = runTest {
        every { amplitude.isBuilt } returns CompletableDeferred(true)

        service.start()

        verify { amplitude.isBuilt }
    }

    @Test
    fun `On start should not touch the opt-out, which the consumer's configuration owns`() = runTest {
        every { amplitude.isBuilt } returns CompletableDeferred(true)

        service.start()

        verify(inverse = true) { amplitude.optOut = any() }
    }

    @Test
    fun `On flush should flush amplitude`() = runTest {
        service.flush()

        verify { amplitude.flush() }
    }

    @Test
    fun `On setEnabled true should opt amplitude in`() = runTest {
        service.setEnabled(true)

        verify { amplitude.optOut = false }
    }

    @Test
    fun `On setEnabled false should opt amplitude out`() = runTest {
        service.setEnabled(false)

        verify { amplitude.optOut = true }
    }

    @Test
    fun `On identify should set the user id`() = runTest {
        service.identify(Identity(USER_ID))

        verify { amplitude.setUserId(USER_ID) }
    }

    @Test
    fun `On reset should reset amplitude`() = runTest {
        service.reset()

        verify { amplitude.reset() }
    }
}
