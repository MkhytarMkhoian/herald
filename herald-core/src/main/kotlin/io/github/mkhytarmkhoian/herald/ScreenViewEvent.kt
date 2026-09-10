package io.github.mkhytarmkhoian.herald

/**
 * A screen becoming visible.
 *
 * [screenName] is separate from [Event.name]: vendors model a screen view as a reserved event
 * whose parameter is the screen name, so the two hold different values.
 */
public interface ScreenViewEvent : Event {
    public val screenName: String
}
