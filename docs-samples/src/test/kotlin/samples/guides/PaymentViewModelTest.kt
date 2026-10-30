package samples.guides

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// --8<-- [start:fake]
// The feature's test fakes its own interface: no Herald, no vendors.
class FakeCheckoutAnalytics : CheckoutAnalytics {
    val checkouts = mutableListOf<Pair<String, Int>>()
    val failedPayments = mutableListOf<String>()

    override suspend fun checkoutStarted(plan: String, seats: Int) {
        checkouts += plan to seats
    }

    override suspend fun paymentFailed(reason: String) {
        failedPayments += reason
    }
}

class PaymentViewModelTest {

    @Test
    fun `a failed payment reports its reason`() = runTest {
        val analytics = FakeCheckoutAnalytics()

        PaymentViewModel(analytics).onPaymentFailed("card_declined")

        assertEquals(listOf("card_declined"), analytics.failedPayments)
        assertTrue(analytics.checkouts.isEmpty())
    }
}
// --8<-- [end:fake]
