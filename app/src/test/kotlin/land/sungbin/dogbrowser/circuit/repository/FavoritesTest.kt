package land.sungbin.dogbrowser.circuit.repository

import app.cash.turbine.turbineScope
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import land.sungbin.dogbrowser.circuit.presenter.Dog
import okio.Path.Companion.toOkioPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.extension.AnnotatedElementContext
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.io.TempDirFactory

class FavoritesTest {
  @field:TempDir(factory = FavoriteDestinationTempFileFactory::class)
  private lateinit var destination: Path

  private lateinit var favorites: Favorites

  @BeforeTest fun prepare() {
    favorites = Favorites(
      fs = FakeFileSystem(),
      destination = destination.toOkioPath(),
      scope = TestScope(UnconfinedTestDispatcher() + Job()),
    )
  }

  @Test fun addFavorites() = runTest {
    favorites += dog("test")
    favorites += dog("test2")
    favorites += dog("test3")

    assertThat(favorites()).containsExactly(
      dog("test"),
      dog("test2"),
      dog("test3"),
    )
  }

  @Test fun removeFavorites() = runTest {
    favorites += dog("test")
    favorites += dog("test2")
    favorites += dog("test3")

    favorites -= dog("test2")
    favorites -= dog("test3")

    assertThat(favorites()).containsExactly(dog("test"))
  }

  @Test fun containsFavorites() = runTest {
    favorites += dog("test")
    favorites += dog("test2")

    assertThat(dog("test") in favorites).isTrue()
    assertThat(dog("test2") in favorites).isTrue()

    assertThat(dog("test3") in favorites).isFalse()
    assertThat(dog("test4") in favorites).isFalse()
  }

  @Test fun observeFavorites() = runTest {
    turbineScope {
      val observes = favorites.observe().testIn(backgroundScope)

      favorites += dog("test")
      assertThat(observes.awaitItem()).containsExactly(dog("test"))

      favorites += dog("test2")
      assertThat(observes.awaitItem()).containsExactly(dog("test"), dog("test2"))

      favorites -= dog("test")
      assertThat(observes.awaitItem()).containsExactly(dog("test2"))

      favorites -= dog("test2")
      assertThat(observes.awaitItem()).isEmpty()

      observes.ensureAllEventsConsumed()
    }
  }

  private fun dog(breed: String) = Dog(breed = breed, image = "", favorite = false)

  class FavoriteDestinationTempFileFactory : TempDirFactory {
    override fun createTempDirectory(
      elementContext: AnnotatedElementContext,
      extensionContext: ExtensionContext,
    ): Path = createTempFile(
      prefix = FavoritesTest::class.simpleName!!,
      suffix = ".${Favorites.DATA_EXTENSION}",
    )
  }
}
