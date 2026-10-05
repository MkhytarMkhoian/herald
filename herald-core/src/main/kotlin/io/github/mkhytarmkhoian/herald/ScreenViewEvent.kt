package io.github.mkhytarmkhoian.herald

/**
 * A screen becoming visible. Its [name] is the screen's name.
 *
 * Vendors with a screen-view event of their own send it that way. To show a different name in one
 * vendor's screen reports, put a factory for that event before the vendor's screen-view factory.
 */
public interface ScreenViewEvent : Event
