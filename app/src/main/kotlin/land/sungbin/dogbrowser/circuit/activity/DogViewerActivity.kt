/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import dagger.hilt.android.AndroidEntryPoint
import land.sungbin.dogbrowser.circuit.presenter.Dog
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
public class DogViewerActivity : ComponentActivity() {
  private val dog by lazy { intent.getParcelableExtra(EXTRA_DOG, Dog::class.java)!! }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT))
    super.onCreate(savedInstanceState)
    setContent {
      ZoomableAsyncImage(
        modifier = Modifier
          .fillMaxSize()
          .background(color = Color.Black),
        model = dog.image,
        contentDescription = dog.breed,
        contentScale = ContentScale.FillWidth,
      )
    }
  }

  public companion object {
    public const val EXTRA_DOG: String = "DogViewerActivity-dog"
  }
}
