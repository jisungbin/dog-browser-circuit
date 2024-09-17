/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.screen

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize
import land.sungbin.dogbrowser.circuit.presenter.Dog

@Parcelize public data object FavoriteDogScreen : Screen {
  public data class State(
    val dogs: ImmutableList<Dog>,
    val eventSink: (Event) -> Unit = {},
  ) : CircuitUiState

  public sealed interface Event : CircuitUiEvent {
    @JvmInline public value class RemoveFavorite(public val dog: Dog) : Event
    @JvmInline public value class GoToViewer(public val dog: Dog) : Event
  }
}
