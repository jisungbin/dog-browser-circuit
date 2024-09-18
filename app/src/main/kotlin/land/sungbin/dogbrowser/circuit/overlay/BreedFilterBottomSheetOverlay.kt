/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.retained.rememberRetained
import com.slack.circuitx.overlays.BottomSheetOverlay

@JvmInline public value class BreedFilters(public val breeds: List<String>)

@OptIn(ExperimentalLayoutApi::class)
@Composable public fun buildBreedFilterBottomSheetOverlay(
  availableBreeds: List<String>,
): Overlay<BreedFilters> {
  val selectedBreeds = rememberRetained { mutableStateListOf<String>() }
  return remember {
    BottomSheetOverlay(
      model = availableBreeds,
      onDismiss = { BreedFilters(selectedBreeds.toList()) },
      content = { availableBreeds, _ ->
        FlowColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(all = 15.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          availableBreeds.fastForEach { breed ->
            BreedFilterChip(
              breed = breed,
              selected = breed in selectedBreeds,
              onClick = {
                if (breed in selectedBreeds)
                  selectedBreeds.remove(breed)
                else
                  selectedBreeds.add(breed)
              },
            )
          }
        }
      },
    )
  }
}

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
