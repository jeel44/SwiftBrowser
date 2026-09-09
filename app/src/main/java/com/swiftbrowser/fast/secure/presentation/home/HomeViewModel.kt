package com.swiftbrowser.fast.secure.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.data.local.dao.SpeedDialDao
import com.swiftbrowser.fast.secure.data.local.entity.SpeedDialEntity
import com.swiftbrowser.fast.secure.data.repository.BookmarkRepository
import com.swiftbrowser.fast.secure.data.repository.NewsRepository
import com.swiftbrowser.fast.secure.domain.model.NewsItem
import com.swiftbrowser.fast.secure.domain.model.SpeedDialSite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.swiftbrowser.fast.secure.BuildConfig
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    @Suppress("UnusedPrivateMember") private val bookmarkRepository: BookmarkRepository,
    private val speedDialDao: SpeedDialDao,
    private val prefs: BrowserPreferences,
) : ViewModel() {

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    val speedDialSites: StateFlow<List<SpeedDialSite>> = speedDialDao
        .getAll()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _newsItems = MutableStateFlow<UiState<List<NewsItem>>>(UiState.Loading)
    val newsItems: StateFlow<UiState<List<NewsItem>>> = _newsItems

    @Suppress("KotlinConstantConditions")
    val isGoogleAdsUser: StateFlow<Boolean> = run {
        // Debug check happens first, before any override/referrer logic, so the Special Offer
        // button is guaranteed visible in debug builds with no exceptions.
        if (BuildConfig.DEBUG) {
            Log.d("SPECIAL_OFFER_DEBUG", "isGoogleAdsUser: path=DEBUG_BUILD_FORCED_TRUE")
            MutableStateFlow(true)
        } else {
            // prefs.isGoogleAdsUser is set exclusively by InstallReferrerChecker's real referrer
            // detection today — there is no separate manual-override preference in this codebase.
            Log.d("SPECIAL_OFFER_DEBUG", "isGoogleAdsUser: path=REAL_REFERRER_DETECTION")
            prefs.isGoogleAdsUser
                .onEach {
                    Log.d("SPECIAL_OFFER_DEBUG", "isGoogleAdsUser: path=REAL_REFERRER_DETECTION emitted $it")
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
        }
    }

    init {
        seedSpeedDialIfEmpty()
        loadNews()
    }

    private fun seedSpeedDialIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (speedDialDao.count() == 0) {
                    speedDialDao.insertAll(
                        Constants.DEFAULT_SPEED_DIAL_SITES.mapIndexed { i, site ->
                            SpeedDialEntity(
                                id = site.id,
                                name = site.name,
                                url = site.url,
                                iconColor = site.iconColor,
                                iconLabel = site.iconLabel,
                                sortOrder = i,
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "HomeViewModel: failed to seed speed dial")
            }
        }
    }

    fun onSpeedDialClick(site: SpeedDialSite) = emit(HomeEvent.NavigateToBrowser(site.url))
    fun onSearchBarClick() = emit(HomeEvent.NavigateToBrowser("https://www.google.com"))
    fun onNewsClick(item: NewsItem) = emit(HomeEvent.NavigateToBrowser(item.url))
    fun onSettingsClick() = emit(HomeEvent.OpenSettings)
    fun retryNews() {
        _newsItems.value = UiState.Loading
        loadNews()
    }

    private fun loadNews() {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.getNews().collect { _newsItems.value = it }
        }
    }

    private fun emit(event: HomeEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    private fun SpeedDialEntity.toDomain() = SpeedDialSite(
        id = id,
        name = name,
        url = url,
        iconColor = iconColor,
        iconLabel = iconLabel,
        drawableRes = when (name.lowercase()) {
            "google"    -> R.drawable.ic_site_google
            "youtube"   -> R.drawable.ic_site_youtube
            "facebook"  -> R.drawable.ic_site_facebook
            "whatsapp"  -> R.drawable.ic_site_whatsapp
            "instagram" -> R.drawable.ic_site_instagram
            "amazon"    -> R.drawable.ic_site_amazon
            else        -> R.drawable.ic_site_google
        },
    )

    sealed class HomeEvent {
        data class NavigateToBrowser(val url: String) : HomeEvent()
        data object OpenSettings : HomeEvent()
    }
}
