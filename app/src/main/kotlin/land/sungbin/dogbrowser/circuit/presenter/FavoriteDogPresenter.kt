/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.internal.rememberStableCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import land.sungbin.dogbrowser.circuit.repository.Favorites
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen
import land.sungbin.dogbrowser.circuit.screen.FavoriteDogScreen

public class FavoriteDogPresenter @AssistedInject constructor(
  private val favorites: Favorites,
  @Assisted private val navigator: Navigator,
) : Presenter<FavoriteDogScreen.State> {
  @Composable override fun present(): FavoriteDogScreen.State {
    val overlay = LocalOverlayHost.current
    val scope = rememberStableCoroutineScope()
    val exceptionHandler = remember(scope, overlay) { OverlayExceptionHandlerKey(scope, overlay) }

    val dogs: ImmutableList<Dog> by produceRetainedState(persistentListOf()) {
      scope.launch(exceptionHandler) {
        favorites.observe().collect { dogs ->
          value = dogs.toPersistentList()
        }
      }
    }

    return FavoriteDogScreen.State(dogs = dogs) { event ->
      when (event) {
        is FavoriteDogScreen.Event.RemoveFavorite -> {
          scope.launch(exceptionHandler) { favorites -= event.dog }
        }
        is FavoriteDogScreen.Event.GoToViewer -> {
          navigator.goTo(DogViewerScreen(event.dog))
        }
      }
    }
  }

  @CircuitInject(FavoriteDogScreen::class, ActivityRetainedComponent::class)
  @AssistedFactory
  public fun interface Factory {
    public fun create(navigator: Navigator): FavoriteDogPresenter
  }
}
