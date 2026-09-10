package io.github.mkhytarmkhoian.herald

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/** Records that work was actually handed to a dispatcher, then runs it for real. */
class RecordingDispatcher : CoroutineDispatcher() {

    @Volatile
    var dispatched: Boolean = false
        private set

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatched = true
        Dispatchers.Default.dispatch(context, block)
    }
}
