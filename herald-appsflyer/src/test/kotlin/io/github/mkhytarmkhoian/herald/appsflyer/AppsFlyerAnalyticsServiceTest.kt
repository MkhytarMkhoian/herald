package io.github.mkhytarmkhoian.herald.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerLib
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AppsFlyerAnalyticsServiceTest {

    private companion object {
        const val USER_ID = "test-user-id"
    }

    private val appsFlyer: AppsFlyerLib = mockk(relaxed = true)
    private val context: Context = mockk()

    private val service = AppsFlyerAnalyticsService(appsFlyer, context)

    @Test
    fun `On start should stop appsflyer and not start it, so a fresh install sends nothing until consent`() = runTest {
        service.start()

        verify { appsFlyer.stop(true, context) }
        verify(exactly = 0) { appsFlyer.start() }
    }

    @Test
    fun `On start should not initialise appsflyer, which the consumer owns`() = runTest {
        service.start()

        verify(inverse = true) { appsFlyer.init(any(), any(), any()) }
    }

    @Test
    fun `On setEnabled true should resume before starting, the order AppsFlyer requires after a stop`() = runTest {
        service.start()
        service.setEnabled(true)

        verifyOrder {
            appsFlyer.stop(true, context)
            appsFlyer.stop(false, context)
            appsFlyer.start()
        }
    }

    @Test
    fun `On setEnabled false should stop appsflyer without starting it`() = runTest {
        service.setEnabled(false)

        verify { appsFlyer.stop(true, context) }
        verify(exactly = 0) { appsFlyer.start() }
    }

    @Test
    fun `On identify should set the customer user id`() = runTest {
        service.identify(Identity(USER_ID))

        verify { appsFlyer.setCustomerUserId(USER_ID) }
    }

    @Test
    fun `On reset should clear the customer user id`() = runTest {
        service.reset()

        verify { appsFlyer.setCustomerUserId(null) }
    }
}
