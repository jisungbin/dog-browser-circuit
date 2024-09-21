/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastDistinctBy
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastMap
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import land.sungbin.dogbrowser.circuit.repository.Dogs
import land.sungbin.dogbrowser.circuit.repository.Favorites
import land.sungbin.dogbrowser.circuit.runSuspendCatching
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen

public class BrowseDogPresenter @AssistedInject constructor(
  private val dogs: Dogs,
  private val favorites: Favorites,
  @Assisted private val navigator: Navigator,
) : Presenter<BrowseDogScreen.State> {
  private suspend fun requestDogs(
    breeds: List<String>,
    count: Int?,
  ): Result<List<Dog>> = coroutineScope {
    val results = (breeds.ifEmpty { listOf(null) }).fastMap { breed ->
      async {
        runSuspendCatching {
          breed to this@BrowseDogPresenter.dogs.images(breed = breed, count = count)
        }
      }
    }
      .awaitAll()
    val exception = results.firstNotNullOfOrNull(Result<*>::exceptionOrNull)

    val dogs = results.fastFilter(Result<*>::isSuccess).fastFlatMap { result ->
      val (breed, images) = result.getOrNull()!!
      images.fastMap { image ->
        Dog(breed = breed, image = image, favorite = false)
      }
    }

    if (dogs.isEmpty()) Result.failure(exception ?: Exception("No dogs found"))
    else Result.success(dogs)
  }

  @Composable override fun present(): BrowseDogScreen.State {
    val scope = rememberCoroutineScope()

    val breedsResult by produceRetainedState(Result.success(persistentListOf())) {
      scope.launch {
        value = runSuspendCatching { this@BrowseDogPresenter.dogs.breeds() }.map(List<String>::toPersistentList)
      }
    }

    var browseDogsResult by rememberRetained { mutableStateOf(Result.success(emptyList<Dog>())) }
    val favoriteDogs by favorites.observe().collectAsRetainedState(emptyList(), context = scope.coroutineContext)

    val dogs by rememberRetained(browseDogsResult) {
      derivedStateOf {
        browseDogsResult.map { browseDogs ->
          browseDogs
            .fastDistinctBy(Dog::image)
            .fastMap { dog -> dog.copy(favorite = favoriteDogs.fastAny { favorite -> favorite.image == dog.image }) }
            .toImmutableList()
        }
      }
    }

    return BrowseDogScreen.State(dogs = dogs, breeds = breedsResult) { event ->
      when (event) {
        is BrowseDogScreen.Event.Browse -> {
          scope.launch { browseDogsResult = requestDogs(event.breeds, event.count) }
        }
        is BrowseDogScreen.Event.AddFavorite -> {
          scope.launch { favorites += event.dog }
        }
        is BrowseDogScreen.Event.RemoveFavorite -> {
          scope.launch { favorites -= event.dog }
        }
        is BrowseDogScreen.Event.GoToViewer -> {
          navigator.goTo(DogViewerScreen(event.dog))
        }
      }
    }
  }

  @CircuitInject(BrowseDogScreen::class, ActivityRetainedComponent::class)
  @AssistedFactory
  public fun interface Factory {
    public fun create(navigator: Navigator): BrowseDogPresenter
  }
}
