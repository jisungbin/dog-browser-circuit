/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExplicitGroupsComposable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dagger.hilt.android.components.ActivityRetainedComponent
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen

@Suppress("ComposableNaming", "NOTHING_TO_INLINE")
@CircuitInject(DogViewerScreen::class, ActivityRetainedComponent::class)
@[Composable ExplicitGroupsComposable]
public inline fun DogViewerPresenter(navigator: Navigator): DogViewerScreen.State =
  DogViewerScreen.State { event ->
    when (event) {
      DogViewerScreen.Event.Pop -> navigator.pop()
    }
  }
