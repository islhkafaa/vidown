package app.vidown.ui.viewmodel

import android.app.Application
import app.vidown.domain.manager.MaintenanceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.repository.AppTheme
import app.vidown.data.repository.SettingsRepository
import app.vidown.data.repository.YtDlpRepository
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

    val downloadUriState: StateFlow<String?> = settingsRepository.downloadUriFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val concurrentDownloadsState: StateFlow<Int> = settingsRepository.concurrentDownloadsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3
        )


    val wifiOnlyState: StateFlow<Boolean> = settingsRepository.wifiOnlyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val autoUpdateExtractorsState: StateFlow<Boolean> = settingsRepository.autoUpdateExtractorsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val concurrentFragmentsState: StateFlow<Int> = settingsRepository.concurrentFragmentsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 4
        )

    val bufferSizeState: StateFlow<String> = settingsRepository.bufferSizeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Standard"
        )

    val forceIpv4State: StateFlow<Boolean> = settingsRepository.forceIpv4Flow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val updateManager = app.vidown.domain.manager.UpdateManager(application)

    private val _updateState =
        kotlinx.coroutines.flow.MutableStateFlow<app.vidown.domain.manager.UpdateResult?>(null)
    val updateState: StateFlow<app.vidown.domain.manager.UpdateResult?> = _updateState

    private val _isCheckingUpdate = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate

    val downloadProgress: StateFlow<Float?> = updateManager.downloadProgress

    private val ytDlpRepository = YtDlpRepository(application)

    private val _isUpdatingExtractors = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isUpdatingExtractors: StateFlow<Boolean> = _isUpdatingExtractors

    private val _extractorUpdateResult =
        kotlinx.coroutines.flow.MutableStateFlow<Result<com.yausername.youtubedl_android.YoutubeDL.UpdateStatus>?>(
            null
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setDownloadUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setDownloadUri(uri)
        }
    }

    fun setConcurrentDownloads(limit: Int) {
        viewModelScope.launch {
            settingsRepository.setConcurrentDownloads(limit)
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
            val result = updateManager.downloadAndInstallUpdate(url, filename)
            if (result) {
                _updateState.value = null
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }

    fun updateExtractors() {
        viewModelScope.launch {
            _isUpdatingExtractors.value = true
            val result = ytDlpRepository.updateYtDlp()
            _extractorUpdateResult.value = result
            _isUpdatingExtractors.value = false
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWifiOnly(enabled)
        }
    }

    fun setAutoUpdateExtractors(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoUpdateExtractors(enabled)
            MaintenanceManager.scheduleExtractorUpdates(
                getApplication<Application>().applicationContext,
                enabled
            )
        }
    }

    fun setConcurrentFragments(count: Int) {
        viewModelScope.launch {
            settingsRepository.setConcurrentFragments(count)
        }
    }

    fun setBufferSize(size: String) {
        viewModelScope.launch {
            settingsRepository.setBufferSize(size)
        }
    }

    fun setForceIpv4(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setForceIpv4(enabled)
        }
    }
}
