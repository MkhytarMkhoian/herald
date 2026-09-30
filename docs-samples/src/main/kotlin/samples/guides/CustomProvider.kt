package samples.guides

import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.Herald
import io.github.mkhytarmkhoian.herald.asString
import kotlin.random.Random

/** Your own collection endpoint. */
interface EventsApi {
    suspend fun send(name: String, parameters: Map<String, String>)
}

// --8<-- [start:backend]
class BackendAnalytics(private val api: EventsApi) : EventTrackerService {
    override suspend fun track(event: Event) {
        api.send(event.name, event.parameters.mapValues { (_, value) -> value.asString })
    }
}
// --8<-- [end:backend]

/** An event that carries personal data and must stay on the device. */
interface PiiEvent : Event

// --8<-- [start:decorators]
/** Only a share of events, for a vendor that bills per event. */
fun EventTrackerService.sampled(rate: Double, random: Random = Random.Default) =
    EventTrackerService { event -> if (random.nextDouble() < rate) track(event) }

/** Everything except what [excluded] matches. */
fun EventTrackerService.except(excluded: (Event) -> Boolean) =
    EventTrackerService { event -> if (!excluded(event)) track(event) }
// --8<-- [end:decorators]

fun register(api: EventsApi, adjustTracker: EventTrackerService) {
    // --8<-- [start:register]
    val herald = Herald {
        provider(name = "backend", events = BackendAnalytics(api).except { it is PiiEvent })
        provider(name = "adjust", events = adjustTracker.sampled(rate = 0.1))
    }
    // --8<-- [end:register]
    herald.hashCode()
}
