/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.retained.rememberRetained
import kotlinx.coroutines.delay

public class ExceptionSnackbarOverlay(private val exception: Throwable) : Overlay<Unit> {
  @Composable override fun Content(navigator: OverlayNavigator<Unit>) {
    var visible by rememberRetained { mutableStateOf(true) }

    LaunchedEffect(navigator) {
      // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material/material/src/commonMain/kotlin/androidx/compose/material/SnackbarHost.kt;l=226;drc=7cee7b650139cfbdc3b580f21af9951556f0a628
      delay(4_000L)
      if (visible) {
        visible = false
        navigator.finish(Unit)
      }
    }

    AnimatedVisibility(
      modifier = Modifier
        .fillMaxSize()
        .wrapContentHeight(Alignment.Bottom),
      visible = visible,
    ) {
      Snackbar(
        modifier = Modifier.fillMaxWidth(),
        dismissAction = {
          TextButton(
            onClick = {
              visible = false
              navigator.finish(Unit)
            },
          ) {
            Text("Close")
          }
        },
      ) {
        Text(exception.message ?: "Unknown error")
      }
    }
  }
}
