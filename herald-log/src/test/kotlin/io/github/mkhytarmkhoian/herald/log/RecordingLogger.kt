package io.github.mkhytarmkhoian.herald.log

/** Records what the adapter printed. */
internal class RecordingLogger : AnalyticsLogger {

    val messages = mutableListOf<String>()

    override fun log(message: String) {
        messages += message
    }
}
