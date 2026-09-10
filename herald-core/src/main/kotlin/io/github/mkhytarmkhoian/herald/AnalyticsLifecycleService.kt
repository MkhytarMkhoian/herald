package io.github.mkhytarmkhoian.herald

/** Start-up and delivery for one vendor. */
public interface AnalyticsLifecycleService {

    /** Initialise the vendor SDK. Call once, from the code that owns the app lifecycle. */
    public suspend fun start()

    /**
     * Best-effort request to deliver anything buffered. Worth calling when consent is revoked or
     * the app goes to the background. Vendors with no flush API keep the default no-op.
     */
    public suspend fun flush() {}
}
