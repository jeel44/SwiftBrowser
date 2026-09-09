package com.swiftbrowser.fast.secure.presentation.history

import com.swiftbrowser.fast.secure.core.base.BaseViewModel
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.base.ViewModelEvent
import com.swiftbrowser.fast.secure.domain.model.HistoryItem
import com.swiftbrowser.fast.secure.domain.usecase.history.ClearHistoryUseCase
import com.swiftbrowser.fast.secure.domain.usecase.history.DeleteHistoryItemUseCase
import com.swiftbrowser.fast.secure.domain.usecase.history.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val deleteHistoryItemUseCase: DeleteHistoryItemUseCase,
) : BaseViewModel<List<HistoryItem>>() {

    init {
        observeHistory()
    }

    private fun observeHistory() {
        launchWithErrorHandling(Dispatchers.IO) {
            getHistoryUseCase().collect { items ->
                _uiState.value = if (items.isEmpty()) UiState.Empty else UiState.Success(items)
            }
        }
    }

    fun onClearHistory() {
        launchWithErrorHandling(Dispatchers.IO) {
            clearHistoryUseCase()
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        launchWithErrorHandling(Dispatchers.IO) {
            deleteHistoryItemUseCase(id)
        }
    }
}

sealed class HistoryEvent : ViewModelEvent() {
    data class OpenHistoryItem(val url: String) : HistoryEvent()
}
