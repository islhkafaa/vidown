package app.vidown.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class SettingsRepository(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val DOWNLOAD_URI_KEY = stringPreferencesKey("download_uri")
        val CONCURRENT_DOWNLOADS_KEY = androidx.datastore.preferences.core.intPreferencesKey("concurrent_downloads")
        val DEFAULT_RESOLUTION_KEY = stringPreferencesKey("default_resolution")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }

    val downloadUriFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[DOWNLOAD_URI_KEY]
        }

    val concurrentDownloadsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[CONCURRENT_DOWNLOADS_KEY] ?: 3
        }

    val defaultResolutionFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_RESOLUTION_KEY] ?: "Best Video"
        }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setDownloadUri(uriString: String?) {
        context.dataStore.edit { preferences ->
            if (uriString == null) {
                preferences.remove(DOWNLOAD_URI_KEY)
            } else {
                preferences[DOWNLOAD_URI_KEY] = uriString
            }
        }
    }

    suspend fun setConcurrentDownloads(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[CONCURRENT_DOWNLOADS_KEY] = limit
        }
    }

    suspend fun setDefaultResolution(resolution: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_RESOLUTION_KEY] = resolution
        }
    }
}
