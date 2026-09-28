package io.github.mkhytarmkhoian.herald.amplitude

import com.amplitude.android.Amplitude
import io.github.mkhytarmkhoian.herald.FallbackFactory
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.Resolution
import io.github.mkhytarmkhoian.herald.amplitude.setters.GenericPropertySetter

/**
 * Sets any property as an Amplitude user property. Claims everything, so it belongs last in a
 * chain.
 *
 * Amplitude has a single store for attributes — the user's — so a `UserProperty` and a plain
 * `Property` go to the same place and this one factory covers both.
 */
public class GenericAmplitudePropertySetterFactory(
    private val amplitude: Amplitude,
) : AmplitudePropertySetterFactory, FallbackFactory {

    override fun create(property: Property): Resolution<AmplitudePropertySetter> =
        Resolution.Claimed(GenericPropertySetter(property, amplitude))
}
