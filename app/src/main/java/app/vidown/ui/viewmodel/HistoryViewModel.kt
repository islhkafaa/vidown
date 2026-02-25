package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.local.AppDatabase
import app.vidown.data.local.HistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    val historyState: StateFlow<List<HistoryEntity>> = db.historyDao().getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            db.historyDao().clearHistory()
        }
    }

    fun deleteRecord(record: HistoryEntity, context: android.content.Context) {
        viewModelScope.launch {
            if (record.fileUri != null) {
                app.vidown.data.repository.MediaStoreManager.deleteFile(context, record.fileUri)
            }
            db.historyDao().deleteById(record.id)
        }
    }
}
