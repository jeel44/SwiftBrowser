package com.swiftbrowser.fast.secure.presentation.bookmarks

import com.swiftbrowser.fast.secure.core.base.BaseViewModel
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.base.ViewModelEvent
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import com.swiftbrowser.fast.secure.domain.usecase.bookmark.DeleteBookmarkUseCase
import com.swiftbrowser.fast.secure.domain.usecase.bookmark.GetBookmarksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
) : BaseViewModel<List<Bookmark>>() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        launchWithErrorHandling(Dispatchers.IO) {
            _searchQuery
                .flatMapLatest { query -> getBookmarksUseCase(query) }
                .collect { bookmarks ->
                    _uiState.value = if (bookmarks.isEmpty()) UiState.Empty else UiState.Success(bookmarks)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onDeleteBookmark(bookmark: Bookmark) {
        launchWithErrorHandling(Dispatchers.IO) {
            deleteBookmarkUseCase(bookmark)
        }
    }
}

sealed class BookmarksEvent : ViewModelEvent() {
    data class OpenBookmark(val url: String) : BookmarksEvent()
}
