/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

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
import land.sungbin.dogbrowser.circuit.overlay.OverlayExceptionHandler
import land.sungbin.dogbrowser.circuit.repository.Dogs
import land.sungbin.dogbrowser.circuit.repository.Favorites
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen

public class BrowseDogPresenter @AssistedInject constructor(
  private val dogs: Dogs,
  private val favorites: Favorites,
  @Assisted private val navigator: Navigator,
) : Presenter<BrowseDogScreen.State> {
  @Composable override fun present(): BrowseDogScreen.State {
    val overlay = LocalOverlayHost.current
    val scope = rememberStableCoroutineScope()
    val exceptionHandler = remember(scope, overlay) { OverlayExceptionHandler(scope, overlay) }

    var dogs: ImmutableList<Dog> by rememberRetained { mutableStateOf(persistentListOf()) }
    val breeds: ImmutableSet<String> by produceRetainedState(persistentSetOf()) {
      scope.launch(exceptionHandler) {
        value = this@BrowseDogPresenter.dogs.breeds().toPersistentSet()
      }
    }

    return BrowseDogScreen.State(dogs = dogs, breeds = breeds) { event ->
      when (event) {
        is BrowseDogScreen.Event.Browse -> {
          scope.launch(exceptionHandler) {
            val images = this@BrowseDogPresenter.dogs.images(breed = event.breed, count = event.count)
            dogs = images.map { image -> Dog(breed = event.breed, image = image) }.toImmutableList()
          }
        }
        is BrowseDogScreen.Event.AddFavorite -> {
          scope.launch(exceptionHandler) { favorites += event.dog }
        }
        is BrowseDogScreen.Event.RemoveFavorite -> {
          scope.launch(exceptionHandler) { favorites -= event.dog }
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
