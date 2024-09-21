/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.screen

import android.content.Context
import android.content.Intent
import androidx.annotation.UiContext
import com.slack.circuitx.android.AndroidScreen
import com.slack.circuitx.android.AndroidScreenStarter
import kotlinx.parcelize.Parcelize
import land.sungbin.dogbrowser.circuit.activity.DogViewerActivity
import land.sungbin.dogbrowser.circuit.presenter.Dog

@Parcelize @JvmInline
public value class DogViewerScreen(public val dog: Dog) : AndroidScreen {
  public companion object {
    public fun buildAndroidStarter(@UiContext context: Context): AndroidScreenStarter =
      AndroidScreenStarter { screen ->
        if (screen is DogViewerScreen) {
          val intent = Intent(context, DogViewerActivity::class.java)
          context.startActivity(intent.putExtra(DogViewerActivity.EXTRA_DOG, screen.dog))
          true
        } else {
          false
        }
      }
  }
}
