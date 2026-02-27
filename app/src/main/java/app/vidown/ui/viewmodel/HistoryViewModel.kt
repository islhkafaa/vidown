package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.local.AppDatabase
import app.vidown.data.local.HistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
  private val db = AppDatabase.getDatabase(application)

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery

  val historyState: StateFlow<List<HistoryEntity>> =
          combine(db.historyDao().getAllHistory(), _searchQuery) { history, query ->
                    if (query.isBlank()) history
                    else history.filter { it.title.contains(query, ignoreCase = true) }
                  }
                  .stateIn(
                          scope = viewModelScope,
                          started = SharingStarted.WhileSubscribed(5000),
                          initialValue = emptyList()
                  )

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun clearHistory() {
    viewModelScope.launch { db.historyDao().clearHistory() }
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
