package io.github.mkhytarmkhoian.herald.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class FirebaseAnalyticsServiceTest {

    companion object {
        private const val USER_ID = "test-user-id"
    }

    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)

    private fun service() = FirebaseAnalyticsService(firebaseAnalytics)

    @Test
    fun `On start should not interact with firebase`() = runTest {
        service().start()

        verify { firebaseAnalytics wasNot Called }
    }

    @Test
    fun `On setEnabled true should enable collection`() = runTest {
        service().setEnabled(true)

        verify { firebaseAnalytics.setAnalyticsCollectionEnabled(true) }
        confirmVerified(firebaseAnalytics)
    }

    @Test
    fun `On setEnabled false should disable collection`() = runTest {
        service().setEnabled(false)

        verify { firebaseAnalytics.setAnalyticsCollectionEnabled(false) }
        confirmVerified(firebaseAnalytics)
    }

    @Test
    fun `On identify should set the user id`() = runTest {
        service().identify(Identity(USER_ID))

        verify { firebaseAnalytics.setUserId(USER_ID) }
        confirmVerified(firebaseAnalytics)
    }

    @Test
    fun `On reset should clear the user id`() = runTest {
        service().reset()

        verify { firebaseAnalytics.setUserId(null) }
        confirmVerified(firebaseAnalytics)
    }
}
