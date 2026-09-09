package com.swiftbrowser.fast.secure.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.swiftbrowser.fast.secure.core.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME,
)

/**
 * Typed wrapper around [DataStore<Preferences>] for all browser settings.
 * Every preference is exposed as a [Flow] so the UI can react to changes in real time.
 */
@Singleton
class BrowserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val dataStore = context.dataStore

    // ──────────────────────────────── Keys ────────────────────────────────

    private object Keys {
        val IS_DATA_SAVER_ENABLED = booleanPreferencesKey("is_data_saver_enabled")
        val IS_DARK_MODE_FOR_SITES = booleanPreferencesKey("is_dark_mode_for_sites")
        val IS_POPUP_BLOCK_ENABLED = booleanPreferencesKey("is_popup_block_enabled")
        val DEFAULT_SEARCH_ENGINE = stringPreferencesKey("default_search_engine")
        val HOMEPAGE_URL = stringPreferencesKey("homepage_url")
        val TEXT_SIZE = intPreferencesKey("text_size")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val SESSION_COUNT = intPreferencesKey("session_count")
        val LAST_DEFAULT_BROWSER_PROMPT = longPreferencesKey("last_default_browser_prompt")
        val IS_AD_BLOCK_ENABLED = booleanPreferencesKey("is_ad_block_enabled")
        val IS_JAVASCRIPT_ENABLED = booleanPreferencesKey("is_javascript_enabled")
        val SAVE_FORM_DATA = booleanPreferencesKey("save_form_data")
        val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val APP_VERSION_CODE = intPreferencesKey("app_version_code")
        val IS_GOOGLE_ADS_USER = booleanPreferencesKey("is_google_ads_user")
        val INSTALL_REFERRER_CHECKED = booleanPreferencesKey("install_referrer_checked")
    }

    // ──────────────────────────────── Flows ────────────────────────────────

    val isDataSaverEnabled: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_DATA_SAVER_ENABLED] ?: false
    }

    val isDarkModeForSites: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_DARK_MODE_FOR_SITES] ?: false
    }

    val isPopupBlockEnabled: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_POPUP_BLOCK_ENABLED] ?: true
    }

    val defaultSearchEngine: Flow<String> = dataStore.data.map {
        it[Keys.DEFAULT_SEARCH_ENGINE] ?: Constants.DEFAULT_SEARCH_ENGINE_KEY
    }

    val homepageUrl: Flow<String> = dataStore.data.map {
        it[Keys.HOMEPAGE_URL] ?: Constants.DEFAULT_HOME_URL
    }

    val textSize: Flow<Int> = dataStore.data.map {
        it[Keys.TEXT_SIZE] ?: Constants.DEFAULT_TEXT_SIZE
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_FIRST_LAUNCH] ?: true
    }

    val sessionCount: Flow<Int> = dataStore.data.map {
        it[Keys.SESSION_COUNT] ?: 0
    }

    val lastDefaultBrowserPrompt: Flow<Long> = dataStore.data.map {
        it[Keys.LAST_DEFAULT_BROWSER_PROMPT] ?: 0L
    }

    val isAdBlockEnabled: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_AD_BLOCK_ENABLED] ?: false
    }

    val isJavaScriptEnabled: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_JAVASCRIPT_ENABLED] ?: true
    }

    val saveFormData: Flow<Boolean> = dataStore.data.map {
        it[Keys.SAVE_FORM_DATA] ?: true
    }

    val notificationPermissionAsked: Flow<Boolean> = dataStore.data.map {
        it[Keys.NOTIFICATION_PERMISSION_ASKED] ?: false
    }

    val selectedLanguage: Flow<String> = dataStore.data.map {
        it[Keys.SELECTED_LANGUAGE] ?: "system"
    }

    val appVersionCode: Flow<Int> = dataStore.data.map {
        it[Keys.APP_VERSION_CODE] ?: 0
    }

    val isGoogleAdsUser: Flow<Boolean> = dataStore.data.map {
        it[Keys.IS_GOOGLE_ADS_USER] ?: false
    }

    val installReferrerChecked: Flow<Boolean> = dataStore.data.map {
        it[Keys.INSTALL_REFERRER_CHECKED] ?: false
    }

    // ──────────────────────────────── Setters ────────────────────────────────

    suspend fun setDataSaverEnabled(enabled: Boolean) = dataStore.edit {
        it[Keys.IS_DATA_SAVER_ENABLED] = enabled
    }

    suspend fun setDarkModeForSites(enabled: Boolean) = dataStore.edit {
        it[Keys.IS_DARK_MODE_FOR_SITES] = enabled
    }

    suspend fun setPopupBlockEnabled(enabled: Boolean) = dataStore.edit {
        it[Keys.IS_POPUP_BLOCK_ENABLED] = enabled
    }

    suspend fun setDefaultSearchEngine(engine: String) = dataStore.edit {
        it[Keys.DEFAULT_SEARCH_ENGINE] = engine
    }

    suspend fun setHomepageUrl(url: String) = dataStore.edit {
        it[Keys.HOMEPAGE_URL] = url
    }

    suspend fun setTextSize(size: Int) = dataStore.edit {
        it[Keys.TEXT_SIZE] = size
    }

    suspend fun setFirstLaunchComplete() = dataStore.edit {
        it[Keys.IS_FIRST_LAUNCH] = false
    }

    suspend fun incrementSessionCount() = dataStore.edit {
        it[Keys.SESSION_COUNT] = (it[Keys.SESSION_COUNT] ?: 0) + 1
    }

    suspend fun setLastDefaultBrowserPrompt(timestamp: Long) = dataStore.edit {
        it[Keys.LAST_DEFAULT_BROWSER_PROMPT] = timestamp
    }

    suspend fun setAdBlockEnabled(enabled: Boolean) = dataStore.edit {
        it[Keys.IS_AD_BLOCK_ENABLED] = enabled
    }

    suspend fun setJavaScriptEnabled(enabled: Boolean) = dataStore.edit {
        it[Keys.IS_JAVASCRIPT_ENABLED] = enabled
    }

    suspend fun setSaveFormData(save: Boolean) = dataStore.edit {
        it[Keys.SAVE_FORM_DATA] = save
    }

    suspend fun setNotificationPermissionAsked(asked: Boolean) = dataStore.edit {
        it[Keys.NOTIFICATION_PERMISSION_ASKED] = asked
    }

    suspend fun setAppVersionCode(code: Int) = dataStore.edit {
        it[Keys.APP_VERSION_CODE] = code
    }

    suspend fun resetFirstLaunch() = dataStore.edit {
        it[Keys.IS_FIRST_LAUNCH] = true
    }

    suspend fun setGoogleAdsUser(isAdsUser: Boolean) = dataStore.edit {
        it[Keys.IS_GOOGLE_ADS_USER] = isAdsUser
    }

    suspend fun setInstallReferrerChecked() = dataStore.edit {
        it[Keys.INSTALL_REFERRER_CHECKED] = true
    }

    suspend fun setSelectedLanguage(code: String) {
        dataStore.edit { it[Keys.SELECTED_LANGUAGE] = code }
        context.getSharedPreferences("swift_lang_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("selected_language", code)
            .apply()
    }
}
