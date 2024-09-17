/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.presenter

import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlinx.parcelize.Parcelize
import okio.Buffer

@Parcelize public data class Dog(
  public val breed: String?,
  public val image: String,
  public val favorite: Boolean,
) : Parcelable {
  public fun toJson(): String {
    val buffer = Buffer()
    JsonWriter.of(buffer).use { writer ->
      writer.beginObject()
      writer.name(Dog::breed.name).value(breed)
      writer.name(Dog::image.name).value(image)
      writer.name(Dog::favorite.name).value(favorite)
      writer.endObject()
    }
    return buffer.readUtf8()
  }

  public companion object {
    public fun fromJson(json: String): Dog {
      var breed: String? = null
      var image: String? = null
      var favorite: Boolean? = null

      JsonReader.of(Buffer().writeUtf8(json)).use { reader ->
        reader.beginObject()
        while (reader.hasNext()) {
          when (reader.nextName()) {
            Dog::breed.name -> breed = reader.nextString()
            Dog::image.name -> image = reader.nextString()
            Dog::favorite.name -> favorite = reader.nextBoolean()
            else -> error("Unexpected key: $this")
          }
        }
        reader.endObject()
      }

      return Dog(
        breed = breed,
        image = checkNotNull(image) { "${Dog::image.name} value is null" },
        favorite = checkNotNull(favorite) { "${Dog::favorite.name} value is null" },
      )
    }
  }
}
