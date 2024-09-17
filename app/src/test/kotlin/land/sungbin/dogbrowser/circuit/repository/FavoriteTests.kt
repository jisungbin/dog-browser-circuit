package land.sungbin.dogbrowser.circuit.repository

import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.test.BeforeTest
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.Path.Companion.toOkioPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.extension.AnnotatedElementContext
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.io.TempDirFactory

class FavoriteTests {
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

  companion object {
    class FavoriteDestinationTempFileFactory : TempDirFactory {
      override fun createTempDirectory(
        elementContext: AnnotatedElementContext,
        extensionContext: ExtensionContext,
      ): Path = createTempFile(
        prefix = FavoriteTests::class.simpleName!!,
        suffix = ".${Favorites.DATA_EXTENSION}",
      )
    }
  }
}
