/*
 * Swift Browser - A premium, private, and secure web browser.
 * Copyright (C) 2026 RebelRoot Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swiftbrowser.fast.secure.privacy

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Owns the manual SOCKS5 proxy credentials (username/password).
 *
 * These are stored inside [EncryptedSharedPreferences] protected by an Android
 * Keystore [MasterKey] (AES-256-GCM) — the same at-rest encryption pattern
 * used for the Safe Locker database key (see `LockerDbKeyManager`). The proxy
 * host/port are not secrets and stay in the regular DataStore prefs; only the
 * username and password (potential credentials to a third-party service) are
 * kept in this encrypted store, so they never land in plaintext prefs, a
 * backup, or a log.
 */
class ProxyCredentialStore(context: Context) {

    companion object {
        private const val PREFS_FILE = "proxy_secure_prefs"
        private const val KEY_USERNAME = "custom_proxy_username"
        private const val KEY_PASSWORD = "custom_proxy_password"
    }

    private val appContext = context.applicationContext

    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getPassword(): String = prefs.getString(KEY_PASSWORD, "") ?: ""

    fun setCredentials(username: String, password: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }
}
