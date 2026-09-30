package samples.guides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import kotlinx.coroutines.launch

/** However your app stores the user's answer: DataStore, SharedPreferences, a backend. */
interface AnalyticsConsentRepository {
    suspend fun isEnabled(): Boolean
    suspend fun setEnabled(enabled: Boolean)
}

// --8<-- [start:record]
class PrivacySettingsViewModel(
    private val consentRepository: AnalyticsConsentRepository,
    private val consent: ConsentService,
) : ViewModel() {
    fun onAnalyticsConsentChanged(enabled: Boolean) {
        viewModelScope.launch {
            consentRepository.setEnabled(enabled) // save the answer first
            consent.setEnabled(enabled)           // then apply it to every vendor
        }
    }
}
// --8<-- [end:record]

suspend fun startAnalytics(
    analytics: AnalyticsLifecycleService,
    consent: ConsentService,
    consentRepository: AnalyticsConsentRepository,
) {
    // --8<-- [start:restore]
    analytics.start()                                 // some vendors start quiet here
    consent.setEnabled(consentRepository.isEnabled()) // so re-apply the stored answer, every launch
    // --8<-- [end:restore]
}
