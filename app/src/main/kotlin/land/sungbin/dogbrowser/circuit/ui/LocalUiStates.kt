package land.sungbin.dogbrowser.circuit.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

public val LocalSnackbarHost: ProvidableCompositionLocal<SnackbarHostState> =
  staticCompositionLocalOf { error("No SnackbarHost provided") }
