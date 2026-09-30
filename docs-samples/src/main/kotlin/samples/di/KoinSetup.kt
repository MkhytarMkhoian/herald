package samples.di

import io.github.mkhytarmkhoian.herald.AnalyticsLifecycleService
import io.github.mkhytarmkhoian.herald.ConsentService
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.IdentifiableUserService
import io.github.mkhytarmkhoian.herald.PropertyTrackerService
import org.koin.dsl.binds
import org.koin.dsl.module

// --8<-- [start:koin]
val analyticsModule = module {
    single {
        Herald {
            providers(getAll<Herald.Provider>()) // each vendor module contributes one
        }
    } binds arrayOf(
        EventTrackerService::class,
        PropertyTrackerService::class,
        IdentifiableUserService::class,
        AnalyticsLifecycleService::class,
        ConsentService::class,
    )
}
// --8<-- [end:koin]
