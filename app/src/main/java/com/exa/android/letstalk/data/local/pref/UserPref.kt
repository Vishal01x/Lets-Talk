package com.exa.android.letstalk.data.local.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val UID_KEY = stringPreferencesKey("user_uid")
    }

    // Save UID
    suspend fun saveUser(uid: String) {
        dataStore.edit { preferences ->
            preferences[UID_KEY] = uid
        }
    }

    // Retrieve UID (Flow)
    val userUidFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[UID_KEY]
    }

    // Retrieve UID synchronously (for splash screen)
    suspend fun getUserUid(): String? {
        return dataStore.data.map { preferences ->
            preferences[UID_KEY]
        }.first()  // Using `first()` to get the current value once (not as Flow)
    }


    // Clear UID (Logout)
    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(UID_KEY)
        }
    }
}
