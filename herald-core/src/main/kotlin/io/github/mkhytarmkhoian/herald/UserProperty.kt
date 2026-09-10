package io.github.mkhytarmkhoian.herald

/**
 * A property describing the person rather than the session.
 *
 * Vendors that keep a user profile store these there instead of attaching them to events.
 * Adapters for vendors that do not treat a [UserProperty] as an ordinary [Property].
 */
public interface UserProperty : Property
