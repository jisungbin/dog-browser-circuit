@file:OptIn(ExperimentalOkHttpApi::class)

package land.sungbin.dogbrowser.circuit.presenter

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isSuccess
import assertk.assertions.prop
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import java.nio.file.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import land.sungbin.dogbrowser.circuit.repository.Dogs
import land.sungbin.dogbrowser.circuit.repository.Favorites
import land.sungbin.dogbrowser.circuit.repository.FavoritesTest.FavoriteDestinationTempFileFactory
import land.sungbin.dogbrowser.circuit.screen.BrowseDogScreen
import land.sungbin.dogbrowser.circuit.screen.DogViewerScreen
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import mockwebserver3.junit5.internal.MockWebServerExtension
import okhttp3.ExperimentalOkHttpApi
import okio.Path.Companion.toOkioPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir

@ExtendWith(MockWebServerExtension::class)
class BrowseDogPresenterTest {
  @field:TempDir(factory = FavoriteDestinationTempFileFactory::class)
  private lateinit var destination: Path

  private lateinit var dogs: Dogs
  private lateinit var favorites: Favorites

  private lateinit var server: MockWebServer
  private lateinit var navigator: FakeNavigator
  private lateinit var presenter: BrowseDogPresenter

  private val defaultDispatcher = dispatcher {
    "/breeds/list/all" respond MockResponse(
      // language=json
      body = """
{
  "message": {
    "hound": []
  },
  "status": "success"
}
          """.trimIndent(),
    )

    "/breeds/image" respond MockResponse(
      // language=json
      body = """
{
  "message": [
    "test.jpg",
    "test2.jpg",
    "test3.jpg"
  ],
  "status": "success"
}
              """.trimIndent(),
    )

    "/breed/hound/images" respond MockResponse(
      // language=json
      body = """
{
  "message": [
    "hound.jpg",
    "hound2.jpg",
    "hound3.jpg"
  ],
  "status": "success"
}
              """.trimIndent(),
    )
  }

  @BeforeTest fun prepare(server: MockWebServer) {
    dogs = Dogs(
      base = server.url("/").toString(),
      dispatcher = UnconfinedTestDispatcher(),
    )
    favorites = Favorites(
      fs = FakeFileSystem(),
      destination = destination.toOkioPath(),
      scope = TestScope(UnconfinedTestDispatcher() + Job()),
    )

    this.server = server
    navigator = FakeNavigator(BrowseDogScreen)
    presenter = BrowseDogPresenter(dogs, favorites, navigator)
  }

  @Test fun presentDogsAndBreedsInitializeState() = runTest {
    server.dispatcher = defaultDispatcher
    presenter.test {
      assertThat(awaitItem()).all {
        prop(BrowseDogScreen.State::dogs).isSuccess().isEmpty()
        prop(BrowseDogScreen.State::breeds).isSuccess().isEmpty()
      }
      assertThat(awaitItem()).all {
        prop(BrowseDogScreen.State::dogs).isSuccess().isEmpty()
        prop(BrowseDogScreen.State::breeds).isSuccess().containsOnly("hound")
      }
      ensureAllEventsConsumed()
    }
  }

  @Test fun presentAllBrowseEventSinked() = runTest {
    server.dispatcher = defaultDispatcher
    presenter.test {
      skipItems(1) // load breeds
      awaitItem().eventSink(BrowseDogScreen.Event.Browse())

      assertThat(awaitItem().dogs)
        .isSuccess()
        .containsExactly(
          dogImage("test.jpg"),
          dogImage("test2.jpg"),
          dogImage("test3.jpg"),
        )

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentHoundBrowseEventSinked() = runTest {
    server.dispatcher = defaultDispatcher
    presenter.test {
      skipItems(1) // load breeds
      awaitItem().eventSink(BrowseDogScreen.Event.Browse(breed = "hound"))

      assertThat(awaitItem().dogs)
        .isSuccess()
        .containsExactly(
          houndImage("hound.jpg"),
          houndImage("hound2.jpg"),
          houndImage("hound3.jpg"),
        )

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentFavoritedBrowseEventSinked() = runTest {
    server.dispatcher = defaultDispatcher
    favorites += dogImage("test.jpg")
    favorites += dogImage("test2.jpg")

    presenter.test {
      skipItems(1) // load breeds
      awaitItem().eventSink(BrowseDogScreen.Event.Browse())

      assertThat(awaitItem().dogs)
        .isSuccess()
        .containsExactly(
          dogImage("test.jpg").copy(favorite = true),
          dogImage("test2.jpg").copy(favorite = true),
          dogImage("test3.jpg"),
        )

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentAddFavoritesEventSinked() = runTest {
    server.dispatcher = defaultDispatcher
    presenter.test {
      skipItems(1) // load breeds
      awaitItem().eventSink(BrowseDogScreen.Event.Browse())

      awaitItem().eventSink(BrowseDogScreen.Event.AddFavorite(dogImage("test.jpg")))
      var event = awaitItem()

      assertThat(event.dogs)
        .isSuccess()
        .containsExactly(
          dogImage("test.jpg").copy(favorite = true),
          dogImage("test2.jpg"),
          dogImage("test3.jpg"),
        )

      event.eventSink(BrowseDogScreen.Event.AddFavorite(dogImage("test2.jpg")))
      event = awaitItem()

      assertThat(event.dogs)
        .isSuccess()
        .containsExactly(
          dogImage("test.jpg").copy(favorite = true),
          dogImage("test2.jpg").copy(favorite = true),
          dogImage("test3.jpg"),
        )

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentRemoveFavoritesEventSinked() = runTest {
    server.dispatcher = defaultDispatcher
    presenter.test {
      skipItems(1) // load breeds
      awaitItem().eventSink(BrowseDogScreen.Event.Browse())

      awaitItem().eventSink(BrowseDogScreen.Event.AddFavorite(dogImage("test.jpg")))
      awaitItem().eventSink(BrowseDogScreen.Event.AddFavorite(dogImage("test2.jpg")))
      awaitItem().eventSink(BrowseDogScreen.Event.AddFavorite(dogImage("test3.jpg")))

      awaitItem().eventSink(BrowseDogScreen.Event.RemoveFavorite(dogImage("test2.jpg")))
      var event = awaitItem()

      assertThat(event.dogs)
        .isSuccess()
        .containsExactly(
          dogImage("test.jpg").copy(favorite = true),
          dogImage("test2.jpg"),
          dogImage("test3.jpg").copy(favorite = true),
        )

      event.eventSink(BrowseDogScreen.Event.RemoveFavorite(dogImage("test3.jpg")))
      event = awaitItem()

      assertThat(event.dogs)
        .isSuccess()
        .containsExactly(
          dogImage("test.jpg").copy(favorite = true),
          dogImage("test2.jpg"),
          dogImage("test3.jpg"),
        )

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentGoToViewerEventSinked() = runTest {
    server.dispatcher = defaultDispatcher
    presenter.test {
      skipItems(1) // load breeds
      awaitItem().eventSink(BrowseDogScreen.Event.Browse())
      awaitItem().eventSink(BrowseDogScreen.Event.GoToViewer(dogImage("test.jpg")))

      assertThat(navigator.awaitNextScreen())
        .isInstanceOf<DogViewerScreen>()
        .prop(DogViewerScreen::dog).isEqualTo(dogImage("test.jpg"))

      ensureAllEventsConsumed()
    }
  }

  @Test fun presentFailedState() = runTest {
    server.dispatcher = dispatcher {
      "/breeds/list/all" respond MockResponse(
        // language=json
        body = """
{
  "message": {},
  "status": "empty"
}
          """.trimIndent(),
      )

      "/breeds/image" respond MockResponse(
        // language=json
        body = """
{
  "message": [],
  "status": "empty-2"
}
              """.trimIndent(),
      )
    }
    presenter.test {
      awaitItem().eventSink(BrowseDogScreen.Event.Browse())
      val event = awaitItem()

      assertThat(event.breeds)
        .isFailure()
        .hasMessage("status is not success: empty")

      assertThat(event.dogs)
        .isFailure()
        .hasMessage("status is not success: empty-2")

      ensureAllEventsConsumed()
    }
  }

  private fun dogImage(image: String) = Dog(breed = null, image = image, favorite = false)
  private fun houndImage(image: String) = Dog(breed = "hound", image = image, favorite = false)

  private fun dispatcher(block: DogDispatcher.() -> Unit): Dispatcher =
    DogDispatcher().apply(block).build()

  private class DogDispatcher {
    private val dispatches = mutableMapOf<String, MockResponse>()

    infix fun String.respond(response: MockResponse) {
      dispatches[this] = response
    }

    fun build() = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        dispatches.getOrElse(request.requestUrl?.encodedPath.orEmpty()) {
          error("No response for ${request.requestUrl}")
        }
    }
  }
}