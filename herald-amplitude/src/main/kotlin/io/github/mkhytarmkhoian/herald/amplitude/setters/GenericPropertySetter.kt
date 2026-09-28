package io.github.mkhytarmkhoian.herald.amplitude.setters

import com.amplitude.android.Amplitude
import com.amplitude.core.events.Identify
import io.github.mkhytarmkhoian.herald.Property
import io.github.mkhytarmkhoian.herald.amplitude.AmplitudePropertySetter
import io.github.mkhytarmkhoian.herald.amplitude.asAmplitudeValue

public class GenericPropertySetter(
    private val property: Property,
    private val amplitude: Amplitude,
) : AmplitudePropertySetter {

    override suspend fun set() {
        amplitude.identify(Identify().set(property.name, property.value.asAmplitudeValue))
    }
}
