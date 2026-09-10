package io.github.mkhytarmkhoian.herald

/** Ties collected data to a known user. */
public interface IdentifiableUserService {

    /** Attach everything sent from now on to [identity]. */
    public suspend fun identify(identity: Identity)

    /** Forget the current user. Call on sign-out. */
    public suspend fun reset()
}
