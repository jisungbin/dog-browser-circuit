/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
public class App : Application(), ImageLoaderFactory {
  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
  }

  override fun newImageLoader(): ImageLoader =
    ImageLoader.Builder(applicationContext)
      .memoryCache {
        MemoryCache.Builder(applicationContext)
          .strongReferencesEnabled(true)
          .maxSizePercent(0.5)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(applicationContext.cacheDir.resolve("image_cache"))
          .build()
      }
      .placeholder(R.drawable.ic_round_photo_24)
      .logger(DebugLogger())
      .build()
}
