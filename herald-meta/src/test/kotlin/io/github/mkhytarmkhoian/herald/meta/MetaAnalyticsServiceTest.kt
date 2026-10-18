package io.github.mkhytarmkhoian.herald.meta

import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import io.github.mkhytarmkhoian.herald.Identity
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class MetaAnalyticsServiceTest {

    private val logger: AppEventsLogger = mockk(relaxed = true)
    private val service = MetaAnalyticsService(logger)

    @Before
    fun setUp() {
        mockkStatic(FacebookSdk::class)
        mockkObject(AppEventsLogger.Companion)
        every { FacebookSdk.setAutoLogAppEventsEnabled(any()) } just runs
        every { FacebookSdk.setAdvertiserIDCollectionEnabled(any()) } just runs
        every { FacebookSdk.setAutoInitEnabled(any()) } just runs
        every { FacebookSdk.fullyInitialize() } just runs
        every { AppEventsLogger.setUserID(any()) } just runs
        every { AppEventsLogger.clearUserID() } just runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `On setEnabled true should turn on Meta's automatic collection and start it`() = runTest {
        service.setEnabled(true)

        verify {
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()
        }
    }

    @Test
    fun `On setEnabled false should turn Meta's automatic collection off`() = runTest {
        service.setEnabled(true)
        service.setEnabled(false)

        verify {
            FacebookSdk.setAutoLogAppEventsEnabled(false)
            FacebookSdk.setAdvertiserIDCollectionEnabled(false)
            FacebookSdk.setAutoInitEnabled(false)
        }
        verify(exactly = 1) { FacebookSdk.fullyInitialize() }
    }

    @Test
    fun `On identify and reset should set and clear Meta's user id`() = runTest {
        service.identify(Identity("user-42"))
        service.reset()

        verify { AppEventsLogger.setUserID("user-42") }
        verify { AppEventsLogger.clearUserID() }
    }

    @Test
    fun `On flush should flush the logger`() = runTest {
        service.flush()

        verify { logger.flush() }
    }
}
