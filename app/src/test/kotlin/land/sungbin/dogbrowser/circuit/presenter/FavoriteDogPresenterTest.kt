/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import java.nio.file.Path
import kotlin.test.BeforeTest
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import land.sungbin.dogbrowser.circuit.repository.Favorites
import land.sungbin.dogbrowser.circuit.repository.FavoritesTest.FavoriteDestinationTempFileFactory
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen
import land.sungbin.dogbrowser.circuit.screen.FavoriteDogScreen
import okio.Path.Companion.toOkioPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class FavoriteDogPresenterTest {
  @field:TempDir(factory = FavoriteDestinationTempFileFactory::class)
  private lateinit var destination: Path

  private lateinit var favorites: Favorites
  private lateinit var navigator: FakeNavigator
  private lateinit var presenter: FavoriteDogPresenter

  @BeforeTest fun prepare() {
    favorites = Favorites(
      fs = FakeFileSystem(),
      destination = destination.toOkioPath(),
      scope = TestScope(UnconfinedTestDispatcher() + Job()),
    )
    navigator = FakeNavigator(BrowseDogScreen)
    presenter = FavoriteDogPresenter(favorites, navigator)
  }

  @Test fun presentFavoriteInitializedEvent() = runTest {
    presenter.test {
      assertThat(awaitItem().dogs).isEmpty()

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentRemoveFavoriteEventSinked() = runTest {
    favorites += dog("test.jpg")
    favorites += dog("test2.jpg")
    presenter.test {
      skipItems(1) // load favorites
      awaitItem().eventSink(FavoriteDogScreen.Event.RemoveFavorite(dog("test.jpg")))

      assertThat(awaitItem().dogs).containsExactly(dog("test2.jpg").copy(favorite = true))

      ensureAllEventsConsumed()
    }
  }

  @Test fun navigateGoToViewerEventSinked() = runTest {
    favorites += dog("test.jpg")
    presenter.test {
      skipItems(1) // load favorites
      awaitItem().eventSink(FavoriteDogScreen.Event.GoToViewer(dog("test.jpg")))

      assertThat(navigator.awaitNextScreen()).isEqualTo(DogViewerScreen(dog("test.jpg")))

      ensureAllEventsConsumed()
    }
  }

  private fun dog(image: String) = Dog(breed = null, image = image, favorite = false)
}
