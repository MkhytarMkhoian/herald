package samples.guides

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import io.github.mkhytarmkhoian.herald.Event
import io.github.mkhytarmkhoian.herald.EventTrackerService
import io.github.mkhytarmkhoian.herald.ScreenViewEvent
import io.github.mkhytarmkhoian.herald.compose.LocalEventTrackerService
import io.github.mkhytarmkhoian.herald.compose.TrackOnLifecycleEvent
import io.github.mkhytarmkhoian.herald.compose.TrackScreenView
import io.github.mkhytarmkhoian.herald.compose.rememberTracker
import io.github.mkhytarmkhoian.herald.compose.trackImpression
import io.github.mkhytarmkhoian.herald.parameters
import kotlin.time.Duration.Companion.seconds

data class MovieDetailsViewed(val movieId: Long) : ScreenViewEvent {
    override val name = "movie_details_viewed"
    override val screenName = "MovieDetails"
    override val parameters = parameters { put("movie_id", movieId) }
}

data class MovieDetailsLeft(val movieId: Long) : Event {
    override val name = "movie_details_left"
}

data class PromoBannerClicked(val promoId: String) : Event {
    override val name = "promo_banner_clicked"
}

data class MovieImpression(val movieId: Long) : Event {
    override val name = "movie_impression"
    override val parameters = parameters { put("movie_id", movieId) }
}

@Composable
fun App() {}

// --8<-- [start:provide]
@Composable
fun AppRoot(tracker: EventTrackerService) { // from Koin, Hilt or a constructor
    CompositionLocalProvider(LocalEventTrackerService provides tracker) {
        App()
    }
}
// --8<-- [end:provide]

// --8<-- [start:screen-view]
@Composable
fun MovieDetailsRoute(movieId: Long) {
    TrackScreenView(remember(movieId) { MovieDetailsViewed(movieId) })
    TrackOnLifecycleEvent(remember(movieId) { MovieDetailsLeft(movieId) }, on = Lifecycle.Event.ON_STOP)
    // ...
}
// --8<-- [end:screen-view]

// --8<-- [start:click]
@Composable
fun PromoBanner(promoId: String) {
    val track = rememberTracker()
    BasicText(
        text = "Try Pro",
        modifier = Modifier.clickable { track(PromoBannerClicked(promoId)) },
    )
}
// --8<-- [end:click]

// --8<-- [start:impression]
@Composable
fun MovieList(movieIds: List<Long>) {
    LazyColumn {
        items(movieIds) { id ->
            BasicText(
                text = "Movie $id",
                // Once per appearance, when half the row has been on screen for a second.
                modifier = Modifier.trackImpression(MovieImpression(id), threshold = 0.5f, minVisibleDuration = 1.seconds),
            )
        }
    }
}
// --8<-- [end:impression]
