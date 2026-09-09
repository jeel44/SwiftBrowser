package com.swiftbrowser.fast.secure.presentation.settings

import com.swiftbrowser.fast.secure.core.base.BaseViewModel
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.base.ViewModelEvent
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * ViewModel for the Settings screen. Reads/writes all preferences via [BrowserPreferences].
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: BrowserPreferences,
) : BaseViewModel<SettingsUiData>() {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        launchWithErrorHandling(Dispatchers.IO) {
            combine(
                preferences.isDataSaverEnabled,
                preferences.isDarkModeForSites,
                preferences.isPopupBlockEnabled,
                preferences.defaultSearchEngine,
                preferences.homepageUrl,
                preferences.textSize,
            ) { values ->
                SettingsUiData(
                    isDataSaverEnabled = values[0] as Boolean,
                    isDarkModeForSites = values[1] as Boolean,
                    isPopupBlockEnabled = values[2] as Boolean,
                    defaultSearchEngine = values[3] as String,
                    homepageUrl = values[4] as String,
                    textSize = values[5] as Int,
                )
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }

    fun setDataSaverEnabled(enabled: Boolean) {
        launchWithErrorHandling(Dispatchers.IO) { preferences.setDataSaverEnabled(enabled) }
    }

    fun setDarkModeForSites(enabled: Boolean) {
        launchWithErrorHandling(Dispatchers.IO) { preferences.setDarkModeForSites(enabled) }
    }

    fun setPopupBlockEnabled(enabled: Boolean) {
        launchWithErrorHandling(Dispatchers.IO) { preferences.setPopupBlockEnabled(enabled) }
    }

    fun setSearchEngine(engine: String) {
        launchWithErrorHandling(Dispatchers.IO) { preferences.setDefaultSearchEngine(engine) }
    }

    fun setHomepageUrl(url: String) {
        launchWithErrorHandling(Dispatchers.IO) { preferences.setHomepageUrl(url) }
    }

    fun setTextSize(size: Int) {
        launchWithErrorHandling(Dispatchers.IO) { preferences.setTextSize(size) }
    }
}

data class SettingsUiData(
    val isDataSaverEnabled: Boolean = false,
    val isDarkModeForSites: Boolean = false,
    val isPopupBlockEnabled: Boolean = true,
    val defaultSearchEngine: String = "google",
    val homepageUrl: String = "https://google.com",
    val textSize: Int = 100,
)

sealed class SettingsEvent : ViewModelEvent()
