/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.popUntil
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.android.rememberAndroidScreenAwareNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import land.sungbin.dogbrowser.circuit.R
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen
import land.sungbin.dogbrowser.circuit.screen.FavoriteDogScreen
import land.sungbin.dogbrowser.circuit.screen.TitledScreen
import land.sungbin.dogbrowser.circuit.ui.LocalSnackbarHost

@AndroidEntryPoint
public class MainActivity : ComponentActivity() {
  @Inject public lateinit var circuit: Circuit

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      CircuitCompositionLocals(circuit) {
        val backstack = rememberSaveableBackStack(BrowseDogScreen)
        val navigator = rememberAndroidScreenAwareNavigator(
          delegate = rememberCircuitNavigator(backstack),
          starter = DogViewerScreen.buildAndroidStarter(this),
        )

        val snackbarHost = remember { SnackbarHostState() }

        MaterialTheme(colorScheme = dynamicThemeScheme()) {
          CompositionLocalProvider(LocalSnackbarHost provides snackbarHost) {
            Scaffold(
              modifier = Modifier.fillMaxSize(),
              topBar = {
                val title by rememberRetained(backstack) {
                  derivedStateOf {
                    val screen = backstack.topRecord?.screen ?: return@derivedStateOf ""
                    if (screen is TitledScreen) screen.title else ""
                  }
                }
                TopAppBar(
                  modifier = Modifier.fillMaxWidth(),
                  title = { Text(title) },
                )
              },
              bottomBar = {
                fun Navigator.popUntilOrGoTo(screen: Screen) {
                  if (screen in peekBackStack()) {
                    popUntil { it == screen }
                  } else {
                    goTo(screen)
                  }
                }

                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                  NavigationBarItem(
                    modifier = Modifier.weight(1f),
                    selected = backstack.topRecord?.screen is BrowseDogScreen,
                    icon = {
                      Icon(
                        painterResource(R.drawable.ic_round_space_dashboard_24),
                        contentDescription = "Browse Dogs",
                      )
                    },
                    label = { Text("Browse") },
                    onClick = { navigator.popUntilOrGoTo(BrowseDogScreen) },
                  )
                  NavigationBarItem(
                    modifier = Modifier.weight(1f),
                    selected = backstack.topRecord?.screen is FavoriteDogScreen,
                    icon = {
                      Icon(
                        painterResource(R.drawable.ic_round_favorite_24),
                        contentDescription = "Favorite Dogs",
                      )
                    },
                    label = { Text("Favorites") },
                    onClick = { navigator.popUntilOrGoTo(FavoriteDogScreen) },
                  )
                }
              },
              snackbarHost = { SnackbarHost(snackbarHost) },
              contentWindowInsets = WindowInsets.statusBars,
            ) { defaultPaddings ->
              ContentWithOverlays {
                NavigableCircuitContent(
                  navigator,
                  backstack,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(defaultPaddings),
                )
              }
            }
          }
        }
      }
    }
  }
}

@[Composable ReadOnlyComposable]
private fun dynamicThemeScheme(darkTheme: Boolean = isSystemInDarkTheme()): ColorScheme {
  val context = LocalContext.current
  return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}
