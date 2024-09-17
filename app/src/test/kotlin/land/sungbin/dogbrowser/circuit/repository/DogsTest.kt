/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

@file:OptIn(ExperimentalOkHttpApi::class)

package land.sungbin.dogbrowser.circuit.repository

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import mockwebserver3.junit5.internal.MockWebServerExtension
import okhttp3.ExperimentalOkHttpApi
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockWebServerExtension::class)
class DogsTest {
  private lateinit var server: MockWebServer
  private lateinit var dogs: Dogs

  @BeforeTest fun prepare(server: MockWebServer) {
    this.server = server
    dogs = Dogs(
      base = server.url("/").toString(),
      dispatcher = UnconfinedTestDispatcher(),
    )
  }

  @Test fun getAllBreedsWithSuccess() = runTest {
    server.enqueue(
      MockResponse(
        // language=json
        body = """
{
  "message": {
    "affenpinscher": [],
    "gaddi": [],
    "hound": [
      "afghan",
      "basset",
      "ibizan",
      "plott",
      "walker"
    ],
    "weimaraner": []
  },
  "status": "success"
}
        """.trimIndent(),
      ),
    )

    assertThat(dogs.breeds()).containsExactly(
      "affenpinscher",
      "gaddi",
      "hound-afghan",
      "hound-basset",
      "hound-ibizan",
      "hound-plott",
      "hound-walker",
      "weimaraner",
    )
  }

  @Test fun getAllBreedsWithFail() = runTest {
    server.enqueue(
      MockResponse(
        // language=json
        body = """
{
  "message": {
    "affenpinscher": [],
    "gaddi": [],
    "hound": [
      "afghan",
      "basset",
      "ibizan",
      "plott",
      "walker"
    ]
  },
  "status": "fail"
}
        """.trimIndent(),
      ),
    )

    assertFailure { dogs.breeds() }.hasMessage("status is not success: fail")
  }

  @Test fun breededAllImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breed/hound/images" -> MockResponse(
            // language=json
            body = """
{
  "message": [
    "test.jpg",
    "test2.jpg",
    "test3.jpg",
    "test4.jpg"
  ],
  "status": "success"
}
            """.trimIndent(),
          )
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images(breed = "hound")).containsExactly(
      "test.jpg",
      "test2.jpg",
      "test3.jpg",
      "test4.jpg",
    )
  }

  @Test fun breededFiveImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breed/hound/images/random/5" -> MockResponse(
            // language=json
            body = """
{
  "message": [
    "test.jpg",
    "test2.jpg",
    "test3.jpg",
    "test4.jpg",
    "test5.jpg"
  ],
  "status": "success"
}
            """.trimIndent(),
          )
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images(breed = "hound", count = 5)).containsExactly(
      "test.jpg",
      "test2.jpg",
      "test3.jpg",
      "test4.jpg",
      "test5.jpg",
    )
  }

  @Test fun randomAllImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breeds/image" -> MockResponse(
            // language=json
            body = """
{
  "message": [
    "test.jpg",
    "test2.jpg",
    "test3.jpg",
    "test4.jpg"
  ],
  "status": "success"
}
            """.trimIndent(),
          )
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images()).containsExactly(
      "test.jpg",
      "test2.jpg",
      "test3.jpg",
      "test4.jpg",
    )
  }

  @Test fun randomFiveImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breeds/image/random/5" -> MockResponse(
            // language=json
            body = """
{
  "message": [
    "test.jpg",
    "test2.jpg",
    "test3.jpg",
    "test4.jpg",
    "test5.jpg"
  ],
  "status": "success"
}
            """.trimIndent(),
          )
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images(count = 5)).containsExactly(
      "test.jpg",
      "test2.jpg",
      "test3.jpg",
      "test4.jpg",
      "test5.jpg",
    )
  }
}
