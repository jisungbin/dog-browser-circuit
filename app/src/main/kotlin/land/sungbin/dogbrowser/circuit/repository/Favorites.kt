/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "ERROR_SUPPRESSION") // FIXME KT-67920

package land.sungbin.dogbrowser.circuit.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataMigration
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import land.sungbin.dogbrowser.circuit.presenter.Dog
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

@Singleton public class Favorites @VisibleForTesting constructor(
  fs: FileSystem,
  destination: Path,
  scope: CoroutineScope,
) {
  @Inject public constructor(@ApplicationContext context: Context) : this(
    fs = FileSystem.SYSTEM,
    destination = context.preferencesDataStoreFile(DATA_DESTINATION).toOkioPath(),
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
  )

  private val dataStore = PreferenceDataStore(
    delegate = PreferenceDataStoreFactory.create(
      storage = OkioStorage(fs, PreferencesSerializer) {
        require(destination.name.endsWith(DATA_EXTENSION)) {
          "File extension for file: ${destination.name} does not match required extension for" +
            " Preferences file: $DATA_EXTENSION"
        }
        destination
      },
      corruptionHandler = null,
      migrations = emptyList<DataMigration<Preferences>>(),
      scope = scope,
    ),
  )

  public suspend operator fun plusAssign(dog: Dog) {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    dataStore.edit { preferences ->
      preferences[KEY_FAVORITES] = favorites + dog.toJson()
    }
  }

  public suspend operator fun minusAssign(dog: Dog) {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    dataStore.edit { preferences ->
      preferences[KEY_FAVORITES] = favorites - dog.toJson()
    }
  }

  public suspend operator fun contains(dog: Dog): Boolean {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    return dog.toJson() in favorites
  }

  public suspend operator fun invoke(): List<Dog> {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    return favorites.map(Dog::fromJson)
  }

  public fun observe(): Flow<List<Dog>> =
    dataStore.data
      .mapNotNull { preferences -> preferences[KEY_FAVORITES] }
      .map { favorites -> favorites.map { json -> Dog.fromJson(json).copy(favorite = true) } }

  public companion object Key {
    private val KEY_FAVORITES = stringSetPreferencesKey("favorites")

    private const val DATA_DESTINATION = "dog-favorites"
    public const val DATA_EXTENSION: String = PreferencesSerializer.fileExtension
  }
}
