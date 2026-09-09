package com.swiftbrowser.fast.secure.presentation.downloads

import com.swiftbrowser.fast.secure.core.base.BaseViewModel
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.base.ViewModelEvent
import com.swiftbrowser.fast.secure.data.repository.DownloadRepository
import com.swiftbrowser.fast.secure.domain.model.DownloadItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * ViewModel for the Downloads screen.
 * Observes [DownloadRepository] and exposes the list via [UiState].
 *
 * TODO(Phase 4): Add delete, open-file, and retry actions.
 */
@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: DownloadRepository,
) : BaseViewModel<List<DownloadItem>>() {

    init {
        observeDownloads()
    }

    private fun observeDownloads() {
        launchWithErrorHandling(Dispatchers.IO) {
            repository.getAll().collect { items ->
                _uiState.value = if (items.isEmpty()) UiState.Empty else UiState.Success(items)
            }
        }
    }
}

sealed class DownloadsEvent : ViewModelEvent()
