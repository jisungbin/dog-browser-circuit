package land.sungbin.dogbrowser.circuit.presenter

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class DogTest {
  @Test fun dogToJson() {
    val dog = Dog("Bulldog", "https://example.com/bulldog.jpg")
    assertThat(dog.toJson()).isEqualTo(
      """
{"breed":"Bulldog","image":"https://example.com/bulldog.jpg"}
      """.trimIndent(),
    )
  }

  @Test fun dogFromJson() {
    val dog = Dog.fromJson(
      """
{"breed":"Bulldog","image":"https://example.com/bulldog.jpg"}
      """.trimIndent(),
    )
    assertThat(dog).isEqualTo(Dog("Bulldog", "https://example.com/bulldog.jpg"))
  }
}