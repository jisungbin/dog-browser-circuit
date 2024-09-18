/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable public fun LazyBreedChip(
  breeds: ImmutableList<String>,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  onBreedClick: (breed: String) -> Unit = {},
) {
  LazyRow(
    modifier = modifier,
    contentPadding = contentPadding,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(items = breeds, key = { breed -> breed }) { breed ->
      BreedChip(
        modifier = Modifier.animateItem(),
        breed = breed,
        onClick = { onBreedClick(breed) },
      )
    }
  }
}

@[Composable NonRestartableComposable]
private fun BreedChip(
  breed: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  FilterChip(
    modifier = modifier,
    selected = true,
    onClick = onClick,
    label = { Text(breed) },
  )
}
