package com.swiftbrowser.fast.secure.presentation.browser

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.EditText

class BrowserChromeClient(
    private val onProgressChanged: (Int) -> Unit,
    private val onFaviconReceived: (Bitmap?) -> Unit,
    private val onShowFileChooser: (ValueCallback<Array<Uri>>, FileChooserParams) -> Boolean,
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        onProgressChanged(newProgress)
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        onFaviconReceived(icon)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        if (filePathCallback == null || fileChooserParams == null) return false
        return onShowFileChooser(filePathCallback, fileChooserParams)
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.deny()
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        val ctx = view?.context ?: run { result?.confirm(); return true }
        AlertDialog.Builder(ctx)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> result?.confirm() }
            .setOnCancelListener { result?.cancel() }
            .show()
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        val ctx = view?.context ?: run { result?.cancel(); return true }
        AlertDialog.Builder(ctx)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> result?.confirm() }
            .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
            .setOnCancelListener { result?.cancel() }
            .show()
        return true
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?,
    ): Boolean {
        val ctx = view?.context ?: run { result?.cancel(); return true }
        val input = EditText(ctx).apply { setText(defaultValue) }
        AlertDialog.Builder(ctx)
            .setMessage(message)
            .setView(input)
            .setPositiveButton("OK") { _, _ -> result?.confirm(input.text.toString()) }
            .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
            .setOnCancelListener { result?.cancel() }
            .show()
        return true
    }
}
