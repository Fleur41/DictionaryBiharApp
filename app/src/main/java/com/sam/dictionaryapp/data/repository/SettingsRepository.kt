package com.sam.dictionaryapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object{
        val THEME_KEY = stringPreferencesKey("app_theme")
    }
    val theme: Flow<String> = dataStore.data.map {
        preferences -> preferences[THEME_KEY] ?: "system"
    }

    suspend fun setTheme(theme: String){
        dataStore.edit{ preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}