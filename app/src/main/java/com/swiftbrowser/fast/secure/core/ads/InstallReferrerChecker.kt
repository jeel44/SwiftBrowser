package com.swiftbrowser.fast.secure.core.ads

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class InstallReferrerChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: BrowserPreferences,
) {

    fun checkIfNeeded(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val alreadyChecked = prefs.installReferrerChecked.first()
            Log.d("AD_REFERRER_DEBUG", "checkIfNeeded: installReferrerChecked=$alreadyChecked")
            if (alreadyChecked) {
                val savedIsAdsUser = prefs.isGoogleAdsUser.first()
                Log.d(
                    "AD_REFERRER_DEBUG",
                    "checkIfNeeded: skipping real check, saved isGoogleAdsUser=$savedIsAdsUser",
                )
                return@launch
            }
            Log.d("AD_REFERRER_DEBUG", "checkIfNeeded: running real referrer check")
            val queryResult = withTimeoutOrNull(10_000L) { queryReferrer() }
            if (queryResult == null) {
                Log.d(
                    "AD_REFERRER_DEBUG",
                    "checkIfNeeded: queryReferrer timed out after 10000ms, defaulting to false",
                )
            }
            val isAdsUser = queryResult ?: false
            prefs.setGoogleAdsUser(isAdsUser)
            prefs.setInstallReferrerChecked()
            Log.d(
                "AD_REFERRER_DEBUG",
                "checkIfNeeded: final decision isGoogleAdsUser=$isAdsUser (saved to prefs)",
            )
            Timber.d("InstallReferrer: isGoogleAdsUser=$isAdsUser")
        }
    }

    private suspend fun queryReferrer(): Boolean = suspendCancellableCoroutine { cont ->
        val client = InstallReferrerClient.newBuilder(context).build()

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                Log.d("AD_REFERRER_DEBUG", "onInstallReferrerSetupFinished: responseCode=$responseCode")
                val isAdsUser = runCatching {
                    responseCode == InstallReferrerClient.InstallReferrerResponse.OK &&
                        client.installReferrer.installReferrer.let { ref ->
                            Log.d("AD_REFERRER_DEBUG", "raw referrerUrl=$ref")
                            Timber.d("InstallReferrer raw: $ref")
                            val params = ref.split("&").associate {
                                val parts = it.split("=", limit = 2)
                                parts[0] to (parts.getOrNull(1) ?: "")
                            }
                            val utmSource = params["utm_source"]
                            val hasGclid = params.containsKey("gclid") && !params["gclid"].isNullOrBlank()
                            val isRealGoogleAdsSource = utmSource == "google" || utmSource == "googleadwords"
                            val isGoogleAdsUser = hasGclid || isRealGoogleAdsSource
                            Log.d(
                                "AD_REFERRER_DEBUG",
                                "parsed utm_source=$utmSource hasGclid=$hasGclid isGoogleAdsUser=$isGoogleAdsUser",
                            )
                            isGoogleAdsUser
                        }
                }.getOrElse { e ->
                    Log.e("AD_REFERRER_DEBUG", "onInstallReferrerSetupFinished: exception reading referrer", e)
                    Timber.e(e, "InstallReferrer: failed to read referrer")
                    false
                }
                Log.d("AD_REFERRER_DEBUG", "onInstallReferrerSetupFinished: resolved isAdsUser=$isAdsUser")
                runCatching { client.endConnection() }
                if (cont.isActive) cont.resume(isAdsUser)
            }

            override fun onInstallReferrerServiceDisconnected() {
                Log.d(
                    "AD_REFERRER_DEBUG",
                    "onInstallReferrerServiceDisconnected: service disconnected, resolving isAdsUser=false",
                )
                if (cont.isActive) cont.resume(false)
            }
        })

        cont.invokeOnCancellation { runCatching { client.endConnection() } }
    }
}
