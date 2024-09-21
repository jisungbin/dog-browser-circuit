/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import land.sungbin.dogbrowser.circuit.overlay.BreedFilterBottomSheetOverlay
import land.sungbin.dogbrowser.circuit.presenter.Dog
import land.sungbin.dogbrowser.circuit.ui.LazyBreedChip
import land.sungbin.dogbrowser.circuit.ui.LazyDogGrid
import land.sungbin.dogbrowser.circuit.ui.LocalSnackbarHost

@Parcelize public data object BrowseDogScreen : TitledScreen {
  override val title: String get() = "Browse Dogs"

  public data class State(
    public val dogs: Result<ImmutableList<Dog>>,
    public val breeds: Result<ImmutableList<String>>,
    public val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  public sealed interface Event : CircuitUiEvent {
    public sealed interface RestrictedFavorites : Event

    public data class Browse(
      public val breeds: List<String> = emptyList(),
      public val count: Int? = null,
    ) : Event

    @JvmInline public value class AddFavorite(public val dog: Dog) : RestrictedFavorites
    @JvmInline public value class RemoveFavorite(public val dog: Dog) : RestrictedFavorites

    @JvmInline public value class GoToViewer(public val dog: Dog) : RestrictedFavorites
  }
}

@CircuitInject(BrowseDogScreen::class, ActivityRetainedComponent::class)
@Composable public fun BrowseDog(
  state: BrowseDogScreen.State,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val overlays = LocalOverlayHost.current
  val snackbarHost = LocalSnackbarHost.current

  val dogs = state.dogs.getOrDefault(persistentListOf())
  val availableBreeds = state.breeds.getOrDefault(persistentListOf())
  val selectedBreeds = rememberRetained { mutableStateListOf<String>() }

  LaunchedEffect(state) {
    if (state.dogs.isFailure) {
      val message = state.dogs.exceptionOrNull()?.message ?: "Unknown error"
      snackbarHost.showSnackbar("Failed to load dogs: $message", withDismissAction = true)
    }

    if (state.breeds.isFailure) {
      val message = state.breeds.exceptionOrNull()?.message ?: "Unknown error"
      snackbarHost.showSnackbar("Failed to load breeds: $message", withDismissAction = true)
    }
  }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextButton(
        onClick = {
          scope.launch {
            overlays.show(BreedFilterBottomSheetOverlay(availableBreeds, selectedBreeds))
            state.eventSink(BrowseDogScreen.Event.Browse(breeds = selectedBreeds))
          }
        },
      ) {
        Text("Breeds filter")
      }
      LazyBreedChip(
        modifier = Modifier.fillMaxWidth(),
        breeds = selectedBreeds,
        contentPadding = PaddingValues(start = 8.dp, end = 16.dp),
        onBreedClick = { breed ->
          if (breed in selectedBreeds) {
            selectedBreeds -= breed
          } else {
            selectedBreeds += breed
          }
        },
      )
    }
    LazyDogGrid(
      modifier = Modifier.fillMaxSize(),
      dogs = dogs,
      contentPadding = PaddingValues(start = 16.dp, bottom = 16.dp, end = 16.dp),
      eventSink = state.eventSink,
    )
  }
}
