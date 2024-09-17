/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/dog-browser-circuit/blob/trunk/LICENSE
 */

package land.sungbin.dogbrowser.circuit.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import land.sungbin.dogbrowser.circuit.presenter.Dog

public interface Favorites {
  public suspend operator fun plusAssign(dog: Dog)
  public suspend operator fun minusAssign(dog: Dog)
  public suspend operator fun contains(dog: Dog): Boolean
  public suspend operator fun invoke(): List<Dog>

  // TODO https://github.com/Kotlin/kotlinx.coroutines/issues/3274
  public fun observe(): Flow<List<Dog>>
}

@Module
@InstallIn(ActivityRetainedComponent::class)
public abstract class FavoritesModule {
  @[Binds ActivityRetainedScoped]
  public abstract fun bindFavorites(real: RealFavorites): Favorites
}

public class RealFavorites @Inject constructor(
  @ApplicationContext private val context: Context,
) : Favorites {
  private val dataStore = PreferenceDataStoreFactory.create {
    context.preferencesDataStoreFile("dog-favorites")
  }

  public override suspend fun plusAssign(dog: Dog) {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    dataStore.edit { preferences ->
      preferences[KEY_FAVORITES] = favorites + dog.toJson()
    }
  }

  public override suspend fun minusAssign(dog: Dog) {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    dataStore.edit { preferences ->
      preferences[KEY_FAVORITES] = favorites - dog.toJson()
    }
  }

  public override suspend fun contains(dog: Dog): Boolean {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    return dog.toJson() in favorites
  }

  public override suspend fun invoke(): List<Dog> {
    val favorites = dataStore.data.first()[KEY_FAVORITES].orEmpty()
    return favorites.map(Dog::fromJson)
  }

  public override fun observe(): Flow<List<Dog>> =
    dataStore.data
      .mapNotNull { preferences -> preferences[KEY_FAVORITES] }
      .map { favorites -> favorites.map(Dog::fromJson) }

  public companion object Key {
    private val KEY_FAVORITES = stringSetPreferencesKey("favorites")
  }
}
