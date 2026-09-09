package com.swiftbrowser.fast.secure.presentation.browser

import androidx.lifecycle.viewModelScope
import com.swiftbrowser.fast.secure.core.base.BaseViewModel
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.base.ViewModelEvent
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.core.utils.toSearchOrUrl
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import com.swiftbrowser.fast.secure.domain.model.BrowserTab
import com.swiftbrowser.fast.secure.domain.model.HistoryItem
import com.swiftbrowser.fast.secure.domain.usecase.bookmark.AddBookmarkUseCase
import com.swiftbrowser.fast.secure.domain.usecase.bookmark.DeleteBookmarkUseCase
import com.swiftbrowser.fast.secure.domain.usecase.bookmark.GetBookmarksUseCase
import com.swiftbrowser.fast.secure.domain.usecase.history.AddHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import com.swiftbrowser.fast.secure.core.ads.RemoteConfigManager

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val addHistoryUseCase: AddHistoryUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val preferences: BrowserPreferences,
) : BaseViewModel<BrowserUiData>() {

    private val _currentTab = MutableStateFlow(BrowserTab())
    val currentTab: StateFlow<BrowserTab> = _currentTab.asStateFlow()

    private val _urlBarText = MutableStateFlow("")
    val urlBarText: StateFlow<String> = _urlBarText.asStateFlow()

    private val _webViewSettings = MutableStateFlow(WebViewSettings())
    val webViewSettings: StateFlow<WebViewSettings> = _webViewSettings.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private var sessionInterstitialCount: Int = 0
    private var lastPageDomain: String = ""
    private var interstitialFiredOnCurrentPage: Boolean = false

    private var bookmarkObserveJob: Job? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        launchWithErrorHandling(Dispatchers.IO) {
            val jsEnabled = preferences.isJavaScriptEnabled.first()
            val dataSaver = preferences.isDataSaverEnabled.first()
            _webViewSettings.value = WebViewSettings(
                javaScriptEnabled = jsEnabled,
                dataSaverEnabled = dataSaver,
                userAgentString = Constants.DEFAULT_USER_AGENT,
            )
            _uiState.value = UiState.Success(BrowserUiData())
        }
    }

    fun setInitialUrl(url: String) {
        val processed = url.toSearchOrUrl()
        _currentTab.value = _currentTab.value.copy(url = processed, isLoading = true)
        _urlBarText.value = processed
    }

    fun onNavigate(input: String) {
        val url = input.toSearchOrUrl()
        _currentTab.value = _currentTab.value.copy(url = url, isLoading = true)
        _urlBarText.value = url
        emitEvent(BrowserEvent.LoadUrl(url))
    }

    fun onPageStarted(url: String) {
        _urlBarText.value = url
        _currentTab.value = _currentTab.value.copy(url = url, isLoading = true, loadProgress = 0)
        checkIsBookmarked(url)
    }

    fun onPageFinished(url: String, title: String, isPrivate: Boolean) {
        _currentTab.value = _currentTab.value.copy(
            url = url,
            title = title,
            isLoading = false,
            loadProgress = 100,
        )
        _urlBarText.value = url

        val skipHistory = isPrivate || url.isBlank() ||
            url.startsWith("about:") || url.startsWith("data:") || url.startsWith("chrome:")
        if (!skipHistory) {
            viewModelScope.launch(Dispatchers.IO) {
                addHistoryUseCase(HistoryItem(title = title.ifBlank { url }, url = url))
            }
        }
    }

    fun onAdTrigger() {
        emitEvent(BrowserEvent.ShowInterstitialAd)
    }

    fun onProgressChanged(progress: Int) {
        _currentTab.value = _currentTab.value.copy(loadProgress = progress)
    }

    fun onUrlBarTextChanged(text: String) {
        _urlBarText.value = text
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _currentTab.value = _currentTab.value.copy(
            canGoBack = canGoBack,
            canGoForward = canGoForward,
        )
    }

    fun toggleBookmark() {
        val tab = _currentTab.value
        if (tab.url.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            if (_isBookmarked.value) {
                val existing = getBookmarksUseCase(tab.url).first()
                    .find { it.url == tab.url }
                existing?.let {
                    deleteBookmarkUseCase(it)
                }
                _isBookmarked.value = false
                emitEvent(BrowserEvent.ShowSnackbar("Bookmark removed"))
            } else {
                addBookmarkUseCase(
                    Bookmark(
                        title = tab.title.ifBlank { tab.url },
                        url = tab.url
                    )
                )
                _isBookmarked.value = true
                emitEvent(BrowserEvent.ShowSnackbar("Bookmarked!"))
            }
        }
    }

    fun shareCurrentUrl() {
        val url = _currentTab.value.url
        if (url.isNotBlank()) emitEvent(BrowserEvent.ShareUrl(url))
    }

    fun onPageNavigated(newUrl: String) {
        val domain = Uri.parse(newUrl).host ?: return
        if (domain == lastPageDomain) return
        interstitialFiredOnCurrentPage = false
        lastPageDomain = domain
        if (RemoteConfigManager.isBrowserInterstitialEnabled() &&
            sessionInterstitialCount < RemoteConfigManager.getBrowserInterstitialMaxPerSession()
        ) {
            interstitialFiredOnCurrentPage = true
            sessionInterstitialCount++
            emitEvent(BrowserEvent.ShowBrowserPageInterstitial)
        }
    }

    private fun checkIsBookmarked(url: String) {
        bookmarkObserveJob?.cancel()
        if (url.isBlank()) {
            _isBookmarked.value = false
            return
        }
        bookmarkObserveJob = viewModelScope.launch(Dispatchers.IO) {
            getBookmarksUseCase().collect { bookmarks ->
                _isBookmarked.value = bookmarks.any { it.url == url }
            }
        }
    }
}

data class BrowserUiData(val placeholder: Unit = Unit)

data class WebViewSettings(
    val javaScriptEnabled: Boolean = true,
    val dataSaverEnabled: Boolean = false,
    val userAgentString: String = Constants.DEFAULT_USER_AGENT,
)

sealed class BrowserEvent : ViewModelEvent() {
    data class LoadUrl(val url: String) : BrowserEvent()
    data object ShowInterstitialAd : BrowserEvent()
    data object ShowBrowserPageInterstitial : BrowserEvent()
    data class ShareUrl(val url: String) : BrowserEvent()
    data class ShowSnackbar(val message: String) : BrowserEvent()
}
