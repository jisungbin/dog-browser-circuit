/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/search-app-circuit/blob/trunk/LICENSE
 */

package land.sungbin.searchapp.circuit.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.launch
import land.sungbin.searchapp.circuit.OverlayExceptionHandler
import land.sungbin.searchapp.circuit.repository.Dogs
import land.sungbin.searchapp.circuit.repository.Favorites
import land.sungbin.searchapp.circuit.screen.DogViewerScreen
import land.sungbin.searchapp.circuit.screen.SearchDogScreen

public class SearchDogPresenter @AssistedInject constructor(
  private val dogs: Dogs,
  private val favorites: Favorites,
  @Assisted private val navigator: Navigator,
) : Presenter<SearchDogScreen.State> {
  @Composable override fun present(): SearchDogScreen.State {
    val overlay = LocalOverlayHost.current
    val scope = rememberStableCoroutineScope()
    val exceptionHandler = remember(scope, overlay) { OverlayExceptionHandler(scope, overlay) }

    var dogs: ImmutableList<Dog> by rememberRetained { mutableStateOf(persistentListOf()) }
    val breeds: ImmutableSet<String> by produceRetainedState(persistentSetOf()) {
      scope.launch(exceptionHandler) {
        value = this@SearchDogPresenter.dogs.breeds().toPersistentSet()
      }
    }

    return SearchDogScreen.State(dogs = dogs, breeds = breeds) { event ->
      when (event) {
        is SearchDogScreen.Event.Search -> {
          scope.launch(exceptionHandler) {
            val images = this@SearchDogPresenter.dogs.images(breed = event.breed, count = event.count)
            dogs = images.map { image -> Dog(breed = event.breed, image = image) }.toImmutableList()
          }
        }
        is SearchDogScreen.Event.AddFavorite -> {
          scope.launch(exceptionHandler) { favorites += event.dog }
        }
        is SearchDogScreen.Event.RemoveFavorite -> {
          scope.launch(exceptionHandler) { favorites -= event.dog }
        }
        is SearchDogScreen.Event.GoToViewer -> {
          navigator.goTo(DogViewerScreen(event.dog))
        }
      }
    }
  }

  @CircuitInject(SearchDogScreen::class, ActivityRetainedComponent::class)
  @AssistedFactory
  public fun interface Factory {
    public fun create(navigator: Navigator): SearchDogPresenter
  }
}
