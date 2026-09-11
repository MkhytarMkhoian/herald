package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class MixpanelAnalyticsServiceTest {

    companion object {
        private const val USER_ID = "test-user-id"
    }

    private val mixpanel: MixpanelAPI = mockk(relaxed = true)

    private fun createMixpanelAnalyticsService(
        loggingEnabled: Boolean = false,
        identificationEnabled: Boolean = true
    ): MixpanelAnalyticsService {
        return MixpanelAnalyticsService(
            mixpanel = mixpanel,
            loggingEnabled = loggingEnabled,
            identificationEnabled = identificationEnabled
        )
    }

    @Before
    fun setup() {
        mockkStatic(MixpanelAPI::flush)
        mockkStatic(MixpanelAPI::setEnableLogging)
        mockkStatic(MixpanelAPI::optOutTracking)
        mockkStatic(MixpanelAPI::reset)
    }

    @After
    fun tearDown() {
        unmockkStatic(MixpanelAPI::flush)
        unmockkStatic(MixpanelAPI::setEnableLogging)
        unmockkStatic(MixpanelAPI::optOutTracking)
        unmockkStatic(MixpanelAPI::reset)
    }

    @Test
    fun `On start should apply the logging setting`() = runTest {
        val service = createMixpanelAnalyticsService()

        service.start()

        verify { mixpanel.setEnableLogging(false) }
    }

    @Test
    fun `On start should not touch the server URL, which the consumer owns`() = runTest {
        val service = createMixpanelAnalyticsService()

        service.start()

        verify(inverse = true) { mixpanel.setServerURL(any()) }
    }

    @Test
    fun `On start should disable reset mixpanel`() = runTest {
        val service = createMixpanelAnalyticsService()

        service.setEnabled(false)

        verify { mixpanel.flush() }
        verify { mixpanel.optOutTracking() }
    }

    @Test
    fun `On start shouldn't reset mixpanel`() = runTest {
        val service = createMixpanelAnalyticsService()

        service.setEnabled(true)

        verify(inverse = true) { mixpanel.flush() }
        verify { mixpanel.optInTracking() }
    }

    @Test
    fun `On invoke reset should call proper mixpanel method`() = runTest {
        val service = createMixpanelAnalyticsService()

        service.reset()

        verify { mixpanel.reset() }
    }

    @Test
    fun `On invoke identify should call proper mixpanel method`() = runTest {
        val service = createMixpanelAnalyticsService()

        val userId = USER_ID
        val identity = Identity(userId)

        service.identify(identity)

        verify { mixpanel.identify(identity.userId, true) }
    }

    @Test
    fun `On invoke reset shouldn't call proper mixpanel method`() = runTest {
        val service = createMixpanelAnalyticsService(identificationEnabled = false)

        service.reset()

        verify(inverse = true) { mixpanel.reset() }
    }

    @Test
    fun `On invoke identify shouldn't call proper mixpanel method`() = runTest {
        val service = createMixpanelAnalyticsService(identificationEnabled = false)

        val userId = USER_ID
        val identity = Identity(userId)

        service.identify(identity)

        verify(inverse = true) { mixpanel.identify(identity.userId, true) }
    }
}
