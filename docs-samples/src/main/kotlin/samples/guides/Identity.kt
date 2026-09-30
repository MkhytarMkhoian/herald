package samples.guides

import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.Identity

/** The app's own session, whatever it is. */
interface Session {
    val userId: String?
}

// --8<-- [start:session]
class AnalyticsIdentity(private val identity: IdentifiableUserService) {

    suspend fun onSignedIn(userId: String) = identity.identify(Identity(userId))

    suspend fun onSignedOut() = identity.reset()

    /** At every cold start: some vendors do not remember the user between launches. */
    suspend fun restore(session: Session) {
        session.userId?.let { identity.identify(Identity(it)) }
    }
}
// --8<-- [end:session]
