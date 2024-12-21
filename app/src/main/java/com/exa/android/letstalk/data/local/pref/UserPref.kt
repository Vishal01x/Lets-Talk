package com.exa.android.letstalk.data.local.pref

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*// Extension property for DataStore
val Context.dataStore by preferenc(name = "user_prefs")

object PreferenceManager {
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    // Save userId
    suspend fun saveUserId(context: Context, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    // Retrieve userId
    fun getUserId(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }
}*/
