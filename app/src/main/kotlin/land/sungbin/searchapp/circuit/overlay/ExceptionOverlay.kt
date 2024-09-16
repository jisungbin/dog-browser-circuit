/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/search-app-circuit/blob/trunk/LICENSE
 */

package land.sungbin.searchapp.circuit.overlay

import androidx.compose.runtime.Composable
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator

public class ExceptionOverlay(private val exception: Throwable) : Overlay<Unit> {
  @Composable override fun Content(navigator: OverlayNavigator<Unit>) {
    TODO("TODO ExceptionOverlay: ${exception.message}")
  }
}
