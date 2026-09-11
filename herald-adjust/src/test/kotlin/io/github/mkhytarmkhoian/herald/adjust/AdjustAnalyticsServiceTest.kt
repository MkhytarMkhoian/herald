package io.github.mkhytarmkhoian.herald.adjust

import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustInstance
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertSame

internal class AdjustAnalyticsServiceTest {

    private companion object {
        const val USER_ID = "test-user-id"
        const val USER_ID_PARAMETER = "user_id"
        const val CUSTOM_ID_PARAMETER = "crm_id"
    }

    private val adjust: AdjustInstance = mockk(relaxed = true)
    private val config: AdjustConfig = mockk(relaxed = true)

    private val adjustAnalyticsService = AdjustAnalyticsService(adjust, config)

    @Test
    fun `On start should hand adjust the config it was given, untouched`() = runTest {
        adjustAnalyticsService.start()

        verify { adjust.initSdk(withArg { assertSame(config, it) }) }
    }

    @Test
    fun `On start should disable adjust after init, so a fresh install collects nothing until consent`() = runTest {
        adjustAnalyticsService.start()

        // After, not before: Adjust ignores a disable() that arrives before initSdk().
        verifyOrder {
            adjust.initSdk(any())
            adjust.disable()
        }
    }

    @Test
    fun `On setEnabled true should enable adjust`() = runTest {
        adjustAnalyticsService.setEnabled(true)

        verify { adjust.enable() }
        verify(exactly = 0) { adjust.disable() }
    }

    @Test
    fun `On setEnabled false should disable adjust`() = runTest {
        adjustAnalyticsService.setEnabled(false)

        verify { adjust.disable() }
        verify(exactly = 0) { adjust.enable() }
    }

    @Test
    fun `On identify should add the user id as a global callback parameter`() = runTest {
        adjustAnalyticsService.identify(Identity(USER_ID))

        verify { adjust.addGlobalCallbackParameter(USER_ID_PARAMETER, USER_ID) }
    }

    @Test
    fun `On reset should remove the parameter identify added, not a different bag`() = runTest {
        adjustAnalyticsService.identify(Identity(USER_ID))
        adjustAnalyticsService.reset()

        verify { adjust.removeGlobalCallbackParameter(USER_ID_PARAMETER) }
    }

    @Test
    fun `On identify with a custom parameter should use it instead of the default`() = runTest {
        val service = AdjustAnalyticsService(adjust, config, CUSTOM_ID_PARAMETER)

        service.identify(Identity(USER_ID))
        service.reset()

        verify { adjust.addGlobalCallbackParameter(CUSTOM_ID_PARAMETER, USER_ID) }
        verify { adjust.removeGlobalCallbackParameter(CUSTOM_ID_PARAMETER) }
        verify(exactly = 0) { adjust.addGlobalCallbackParameter(USER_ID_PARAMETER, any()) }
    }
}
