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
      MockResponse.Builder()
        .body(
// language=json
          """
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
        )
        .build(),
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
      MockResponse.Builder()
        .body(
// language=json
          """
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
        )
        .build(),
    )

    assertFailure { dogs.breeds() }.hasMessage("status is not success: fail")
  }

  @Test fun breededAllImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breed/hound/images" -> MockResponse.Builder()
            .body(
// language=json
              """
{
  "message": [
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg"
  ],
  "status": "success"
}
              """.trimIndent(),
            )
            .build()
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images(breed = "hound")).containsExactly(
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg",
    )
  }

  @Test fun breededFiveImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breed/hound/images/random/5" -> MockResponse.Builder()
            .body(
// language=json
              """
{
  "message": [
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_11536.jpg"
  ],
  "status": "success"
}
              """.trimIndent(),
            )
            .build()
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images(breed = "hound", count = 5)).containsExactly(
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_11536.jpg",
    )
  }

  @Test fun randomAllImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breeds/image" -> MockResponse.Builder()
            .body(
// language=json
              """
{
  "message": [
    "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4053.jpg",
    "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4054.jpg",
    "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4055.jpg",
    "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4056.jpg"
  ],
  "status": "success"
}
              """.trimIndent(),
            )
            .build()
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images()).containsExactly(
      "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4053.jpg",
      "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4054.jpg",
      "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4055.jpg",
      "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_4056.jpg",
    )
  }

  @Test fun randomFiveImages() = runTest {
    server.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse =
        when (request.requestUrl?.encodedPath) {
          "/breeds/image/random/5" -> MockResponse.Builder()
            .body(
// language=json
              """
{
  "message": [
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg",
    "https://images.dog.ceo/breeds/hound-afghan/n02088094_11536.jpg"
  ],
  "status": "success"
}
              """.trimIndent(),
            )
            .build()
          else -> fail("Unexpected request: $this")
        }
    }

    assertThat(dogs.images(count = 5)).containsExactly(
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_10263.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_115.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_1150.jpg",
      "https://images.dog.ceo/breeds/hound-afghan/n02088094_11536.jpg",
    )
  }
}
