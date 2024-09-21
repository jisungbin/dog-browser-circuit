/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.slack.circuit.overlay.Overlay
import com.slack.circuitx.overlays.BottomSheetOverlay

@Suppress("FunctionName")
public fun BreedFilterBottomSheetOverlay(
  availableBreeds: List<String>,
  selectedBreed: State<String?>,
  onClick: (breed: String) -> Unit,
): Overlay<Unit> =
  BottomSheetOverlay(
    model = availableBreeds,
    onDismiss = {},
    content = { availableBreeds, _ ->
      if (availableBreeds.isEmpty()) {
        BreedsEmpty(
          modifier = Modifier
            .fillMaxSize(fraction = 0.3f)
            .wrapContentSize(),
        )
      } else {
        FlowColumn(
          modifier = Modifier
            .fillMaxSize(fraction = 0.65f)
            .padding(all = 15.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          availableBreeds.fastForEach { breed ->
            BreedFilterChip(
              breed = breed,
              selected = breed == selectedBreed.value,
              onClick = { onClick(breed) },
            )
          }
        }
      }
    },
  )

@[Composable NonRestartableComposable]
private fun BreedFilterChip(
  breed: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  FilterChip(
    modifier = modifier,
    selected = selected,
    label = { Text(breed) },
    onClick = onClick,
  )
}

@Composable private fun BreedsEmpty(modifier: Modifier = Modifier) {
  Text(
    modifier = modifier,
    text = "No breeds found.",
    style = MaterialTheme.typography.headlineLarge,
  )
}