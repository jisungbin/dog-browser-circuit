/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.repository

import com.squareup.moshi.JsonReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSource
import timber.log.Timber

@Singleton public class Dogs(
  base: String,
  private val dispatcher: CoroutineDispatcher,
) {
  @Inject public constructor() : this(
    base = "https://dog.ceo/api",
    dispatcher = Dispatchers.IO,
  )

  private val base = base.toHttpUrl()
  private val client = OkHttpClient.Builder()
    .addInterceptor(
      HttpLoggingInterceptor { message -> Timber.tag(TAG).d(message) }
        .setLevel(HttpLoggingInterceptor.Level.BASIC),
    )
    .build()

  @Throws(IOException::class, IllegalStateException::class)
  public suspend fun breeds(): List<String> {
    val result = withContext(dispatcher) {
      val url = base.newBuilder().addPathSegments("breeds/list/all").build()
      client.newCall(Request.Builder().url(url).build()).execute()
    }
    check(!result.isSuccessful) { "Failed to fetch breeds: ${result.code}" }
    return result.body.source().parseBreeds()
  }

  @Throws(IOException::class, IllegalStateException::class)
  public suspend fun images(
    breed: String? = null,
    count: Int? = null,
  ): List<String> {
    val result = withContext(dispatcher) {
      val url = base.newBuilder()
        .addPathSegments(if (breed == null) "breeds" else "breed/$breed")
        .addPathSegment(if (breed == null) "image" else "images")
        .addPathSegment("random")
        .apply { if (count != null) addPathSegment(count.toString()) }
        .build()
      client.newCall(Request.Builder().url(url).build()).execute()
    }
    check(!result.isSuccessful) { "Failed to fetch images: ${result.code}" }
    return result.body.source().parseImages()
  }

  @Throws(IllegalStateException::class)
  private fun BufferedSource.parseBreeds(): List<String> = buildList {
    JsonReader.of(this@parseBreeds).use { reader ->
      reader.beginObject()
      while (reader.hasNext()) {
        when (reader.nextName()) {
          "message" -> {
            reader.beginObject()
            while (reader.hasNext()) {
              val subject = reader.nextName()
              reader.beginArray()
              if (reader.hasNext()) {
                while (reader.hasNext()) {
                  add("$subject-${reader.nextString()}")
                }
              } else {
                add(subject)
              }
              reader.endArray()
            }
            reader.endObject()
          }
          "status" -> {
            val status = reader.nextString()
            check(status == "success") { "status is not success: $status" }
          }
          else -> reader.skipValue()
        }
      }
      reader.endObject()
    }
  }

  @Throws(IllegalStateException::class)
  private fun BufferedSource.parseImages(): List<String> = buildList {
    JsonReader.of(this@parseImages).use { reader ->
      reader.beginObject()
      while (reader.hasNext()) {
        when (reader.nextName()) {
          "message" -> {
            reader.beginArray()
            while (reader.hasNext()) {
              add(reader.nextString())
            }
            reader.endArray()
          }
          "status" -> {
            val status = reader.nextString()
            check(status == "success") { "status is not success: $status" }
          }
          else -> reader.skipValue()
        }
      }
      reader.endObject()
    }
  }

  public companion object {
    private const val TAG = "DOG-REPOSITORY"
  }
}
