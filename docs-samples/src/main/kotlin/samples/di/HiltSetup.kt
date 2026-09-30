package samples.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.PropertyTrackerService
import javax.inject.Singleton

// --8<-- [start:hilt]
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun herald(providers: Set<@JvmSuppressWildcards Herald.Provider>): Herald = Herald {
        providers(providers) // each vendor module contributes one with @IntoSet
    }

    @Provides fun events(herald: Herald): EventTrackerService = herald
    @Provides fun properties(herald: Herald): PropertyTrackerService = herald
    @Provides fun identity(herald: Herald): IdentifiableUserService = herald
    @Provides fun lifecycle(herald: Herald): AnalyticsLifecycleService = herald
    @Provides fun consent(herald: Herald): ConsentService = herald
}
// --8<-- [end:hilt]
