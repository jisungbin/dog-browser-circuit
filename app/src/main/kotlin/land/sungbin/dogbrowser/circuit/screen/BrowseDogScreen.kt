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

@Parcelize public data object BrowseDogScreen : Screen {
  public data class State(
    public val dogs: Result<ImmutableList<Dog>>,
    public val breeds: Result<ImmutableList<String>>,
    public val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  public sealed interface Event : CircuitUiEvent {
    public data class Browse(
      public val breed: String? = null,
      public val count: Int? = null,
    ) : Event

    @JvmInline public value class AddFavorite(public val dog: Dog) : Event
    @JvmInline public value class RemoveFavorite(public val dog: Dog) : Event

    @JvmInline public value class GoToViewer(public val dog: Dog) : Event
  }
}
