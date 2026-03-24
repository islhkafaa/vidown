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
        val CONCURRENT_DOWNLOADS_KEY =
            androidx.datastore.preferences.core.intPreferencesKey("concurrent_downloads")
        val WIFI_ONLY_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("wifi_only")
        val AUTO_UPDATE_EXTRACTORS_KEY =
            androidx.datastore.preferences.core.booleanPreferencesKey("auto_update_extractors")
        val CONCURRENT_FRAGMENTS_KEY =
            androidx.datastore.preferences.core.intPreferencesKey("concurrent_fragments")
        val BUFFER_SIZE_KEY = stringPreferencesKey("buffer_size")
        val FORCE_IPV4_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("force_ipv4")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            try {
                AppTheme.valueOf(themeName)
            } catch (_: IllegalArgumentException) {
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


    val wifiOnlyFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[WIFI_ONLY_KEY] ?: false
        }

    val autoUpdateExtractorsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_UPDATE_EXTRACTORS_KEY] ?: true
        }

    val concurrentFragmentsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[CONCURRENT_FRAGMENTS_KEY] ?: 4
        }

    val bufferSizeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[BUFFER_SIZE_KEY] ?: "Standard"
        }

    val forceIpv4Flow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FORCE_IPV4_KEY] ?: false
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

    suspend fun setWifiOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_ONLY_KEY] = enabled
        }
    }

    suspend fun setAutoUpdateExtractors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_UPDATE_EXTRACTORS_KEY] = enabled
        }
    }

    suspend fun setConcurrentFragments(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[CONCURRENT_FRAGMENTS_KEY] = count
        }
    }

    suspend fun setBufferSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[BUFFER_SIZE_KEY] = size
        }
    }

    suspend fun setForceIpv4(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FORCE_IPV4_KEY] = enabled
        }
    }
}
