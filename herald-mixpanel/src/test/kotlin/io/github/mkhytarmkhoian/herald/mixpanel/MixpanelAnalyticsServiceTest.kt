package io.github.mkhytarmkhoian.herald.mixpanel

import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.Called
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

    private fun createMixpanelAnalyticsService() = MixpanelAnalyticsService(mixpanel)

    @Before
    fun setup() {
        mockkStatic(MixpanelAPI::flush)
        mockkStatic(MixpanelAPI::optOutTracking)
        mockkStatic(MixpanelAPI::reset)
    }

    @After
    fun tearDown() {
        unmockkStatic(MixpanelAPI::flush)
        unmockkStatic(MixpanelAPI::optOutTracking)
        unmockkStatic(MixpanelAPI::reset)
    }

    @Test
    fun `On start should not touch mixpanel, whose settings the consumer owns`() = runTest {
        val service = createMixpanelAnalyticsService()

        service.start()

        verify { mixpanel wasNot Called }
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
}
