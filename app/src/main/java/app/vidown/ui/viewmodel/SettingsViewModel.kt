package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.repository.AppTheme
import app.vidown.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)

    val themeState: StateFlow<AppTheme> = settingsRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    private val updateManager = app.vidown.domain.manager.UpdateManager(application)

    private val _updateState = kotlinx.coroutines.flow.MutableStateFlow<app.vidown.domain.manager.UpdateResult?>(null)
    val updateState: StateFlow<app.vidown.domain.manager.UpdateResult?> = _updateState

    private val _isCheckingUpdate = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val result = updateManager.checkForUpdates(currentVersion)
            _updateState.value = result
            _isCheckingUpdate.value = false
        }
    }

    fun downloadUpdate(url: String, filename: String) {
        viewModelScope.launch {
            updateManager.downloadAndInstallUpdate(url, filename)
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }
}
