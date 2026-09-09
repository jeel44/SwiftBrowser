package com.swiftbrowser.fast.secure.presentation.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import timber.log.Timber

class BrowserWebViewClient(
    private val context: Context,
    private val onPageStarted: (url: String) -> Unit,
    private val onPageFinished: (url: String, title: String) -> Unit,
    private val onPageError: (code: Int, description: String) -> Unit,
    private val shouldShowAd: () -> Boolean,
    private val onAdTrigger: () -> Unit,
) : WebViewClient() {

    private var pageLoadCount = 0
    private var isError = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        isError = false
        url?.let { onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (isError) return
        url?.let {
            onPageFinished(it, view?.title ?: it)
        }
        pageLoadCount++
        if (pageLoadCount % 4 == 0 && shouldShowAd()) {
            onAdTrigger()
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (request?.isForMainFrame == true) {
            isError = true
            onPageError(error?.errorCode ?: -1, error?.description?.toString() ?: "Unknown error")
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        handler?.cancel()
        onPageError(-2, "SSL certificate error")
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        return handleSpecialScheme(url)
    }

    private fun handleSpecialScheme(url: String): Boolean {
        return when {
            url.startsWith("intent://") -> {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to handle intent:// URL")
                }
                true
            }
            url.startsWith("market://") -> {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e: ActivityNotFoundException) {
                    Timber.w("Play Store not available for: $url")
                }
                true
            }
            url.startsWith("tel:") || url.startsWith("mailto:") ||
            url.startsWith("sms:") || url.startsWith("whatsapp:") -> {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e: ActivityNotFoundException) {
                    Timber.w("No handler for scheme: $url")
                }
                true
            }
            else -> false
        }
    }
}

fun WebView.setupDownloadListener(
    context: Context,
    onDownloadStarted: (fileName: String) -> Unit,
) {
    setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                setTitle(fileName)
                setDescription("Downloading via Swift Browser")
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                addRequestHeader("User-Agent", userAgent)
                addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            onDownloadStarted(fileName)
        } catch (e: Exception) {
            Timber.e(e, "Download failed for URL: $url")
        }
    }
}
