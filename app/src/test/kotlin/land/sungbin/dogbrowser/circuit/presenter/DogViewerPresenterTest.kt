/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.FakeNavigator.PopEvent
import com.slack.circuit.test.test
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen

class DogViewerPresenterTest {
  private lateinit var navigator: FakeNavigator
  private lateinit var presenter: Presenter<DogViewerScreen.State>

  @BeforeTest fun prepare() {
    navigator = FakeNavigator(BrowseDogScreen, DogViewerScreen(dog("hound")))
    presenter = presenterOf { DogViewerPresenter(navigator) }
  }

  @Test fun navigatePopEventSinked() = runTest {
    presenter.test {
      awaitItem().eventSink(DogViewerScreen.Event.Pop)

      assertThat(navigator.awaitPop())
        .prop(PopEvent::poppedScreen)
        .isEqualTo(DogViewerScreen(dog("hound")))
    }
  }

  @Suppress("SameParameterValue")
  private fun dog(breed: String) = Dog(breed = breed, image = "", favorite = false)
}
