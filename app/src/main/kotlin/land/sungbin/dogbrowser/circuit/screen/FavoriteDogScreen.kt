/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize
import land.sungbin.dogbrowser.circuit.presenter.Dog
import land.sungbin.dogbrowser.circuit.ui.LazyDogGrid

@Parcelize public data object FavoriteDogScreen : TitledScreen {
  override val title: String get() = "Favorite Dogs"

  public data class State(
    val dogs: ImmutableList<Dog>,
    val eventSink: (BrowseDogScreen.Event.RestrictedFavorites) -> Unit = {},
  ) : CircuitUiState
}

@CircuitInject(FavoriteDogScreen::class, ActivityRetainedComponent::class)
@[Composable NonRestartableComposable]
public fun FavoriteDogs(
  state: FavoriteDogScreen.State,
  modifier: Modifier = Modifier,
) {
  LazyDogGrid(
    modifier = modifier,
    dogs = state.dogs,
    contentPadding = PaddingValues(all = 16.dp),
    eventSink = state.eventSink,
  )
}
