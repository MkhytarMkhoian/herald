package io.github.mkhytarmkhoian.herald.firebase

import com.google.firebase.analytics.FirebaseAnalytics
import io.github.mkhytarmkhoian.herald.Resolution
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertFailsWith

class CompositeFirebaseEventTrackerFactoryChainTest {

    private val analytics: FirebaseAnalytics = mockk(relaxed = true)
    private val partial = FirebaseEventTrackerFactory { Resolution.Declined }

    @Test
    fun `given the generic factory not last when building the chain then it is refused`() {
        assertFailsWith<IllegalArgumentException> {
            CompositeFirebaseEventTrackerFactory(GenericFirebaseEventTrackerFactory(analytics), partial)
        }
    }

    @Test
    fun `given both a generic and a require-mapped factory when building the chain then it is refused`() {
        assertFailsWith<IllegalArgumentException> {
            CompositeFirebaseEventTrackerFactory(
                partial,
                RequireMappedFirebaseEventTrackerFactory,
                GenericFirebaseEventTrackerFactory(analytics),
            )
        }
    }

    @Test
    fun `given the generic factory last when building the chain then it is accepted`() {
        CompositeFirebaseEventTrackerFactory(partial, ScreenViewFirebaseEventTrackerFactory(analytics), GenericFirebaseEventTrackerFactory(analytics))
    }
}
