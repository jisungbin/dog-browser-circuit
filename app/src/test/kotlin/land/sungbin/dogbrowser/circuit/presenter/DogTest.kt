package land.sungbin.dogbrowser.circuit.presenter

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import kotlin.test.Test

class DogTest {
  @Test fun dogToJson() {
    val dog = Dog("Bulldog", "https://example.com/bulldog.jpg", false)
    assertThat(dog.toJson()).isEqualTo(
      """
{"breed":"Bulldog","image":"https://example.com/bulldog.jpg","favorite":false}
      """.trimIndent(),
    )
  }

  @Test fun dogFromJson() {
    val dog = Dog.fromJson(
      """
{"breed":"Bulldog","image":"https://example.com/bulldog.jpg","favorite":true}
      """.trimIndent(),
    )
    assertThat(dog).isDataClassEqualTo(Dog("Bulldog", "https://example.com/bulldog.jpg", true))
  }
}