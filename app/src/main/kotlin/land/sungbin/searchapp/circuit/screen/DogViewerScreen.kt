/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/search-app-circuit/blob/trunk/LICENSE
 */

package land.sungbin.searchapp.circuit.screen

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import land.sungbin.searchapp.circuit.presenter.Dog

@Parcelize public class DogViewerScreen(public val dog: Dog) : Screen {
  @JvmInline public value class State(public val eventSink: (Event) -> Unit = {}) : CircuitUiState

  public sealed interface Event : CircuitUiState {
    public data object Pop : Event
  }
}
