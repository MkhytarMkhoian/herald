package samples.di

import io.github.mkhytarmkhoian.herald.Herald
import samples.CheckoutViewModel

// --8<-- [start:manual]
class AppGraph(private val herald: Herald) {
    // CheckoutViewModel asks for an EventTrackerService; Herald is one, so it is passed as that.
    fun checkoutViewModel() = CheckoutViewModel(analytics = herald)
}
// --8<-- [end:manual]
