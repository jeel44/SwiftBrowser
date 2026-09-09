package com.swiftbrowser.fast.secure.presentation.browser

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import com.swiftbrowser.fast.secure.BuildConfig
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.AdManager
import com.swiftbrowser.fast.secure.core.ads.BannerAdView
import com.swiftbrowser.fast.secure.core.utils.toDisplayUrl
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme
import com.swiftbrowser.fast.secure.presentation.theme.SwiftGreen
import com.swiftbrowser.fast.secure.presentation.theme.SwiftPurple
import com.swiftbrowser.fast.secure.presentation.theme.SwiftRed
import kotlinx.coroutines.launch

// ─── Design tokens ───────────────────────────────────────────────────────────
private val BrowserBg           = Color(0xFF111111)
private val BrowserTopBarBg     = Color(0xFF1A1A1A)
private val BrowserBottomBg     = Color(0xFF1A1A1A)
private val BrowserUrlBarBg     = Color(0xFF2A2A2A)
private val BrowserPurple       = Color(0xFF6C47D9)
private val BrowserProgressColor = Color(0xFF9B7FE8)
private val BrowserMenuBg       = Color(0xFF2A2A2A)
private val BrowserDivider      = Color(0xFF2C2C2C)
private val IconActive          = Color(0xFFFFFFFF)
private val IconInactive        = Color(0xFF9E9E9E)
private val IconDisabled        = Color(0xFF404040)

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    initialUrl: String,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val currentTab by viewModel.currentTab.collectAsState()
    val webViewSettings by viewModel.webViewSettings.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()

    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    var isUrlBarEditing by rememberSaveable { mutableStateOf(false) }
    var urlBarValue by remember { mutableStateOf(TextFieldValue("")) }
    val urlFocusRequester = remember { FocusRequester() }

    var pageError by remember { mutableStateOf<String?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var fileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileChooserCallback?.onReceiveValue(uris)
        fileChooserCallback = null
    }

    val pullToRefreshState = rememberPullToRefreshState()
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isImeVisible = imeBottom > 0

    LaunchedEffect(isUrlBarEditing) {
        if (isUrlBarEditing) urlFocusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BrowserEvent.LoadUrl -> webViewRef.value?.loadUrl(event.url)
                is BrowserEvent.ShowInterstitialAd -> activity?.let {
                    if (!isUrlBarEditing && !currentTab.isLoading) {
                        AdManager.showBrowserInterstitial(it) { }
                    }
                }
                is BrowserEvent.ShowBrowserPageInterstitial -> activity?.let {
                    AdManager.showBrowserInterstitial(it) { }
                }
                is BrowserEvent.ShareUrl -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.url)
                    }
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.browser_share_chooser_title))
                    )
                }
                is BrowserEvent.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(initialUrl) {
        if (initialUrl.isNotBlank()) viewModel.setInitialUrl(initialUrl)
    }

    BackHandler(enabled = currentTab.canGoBack) { webViewRef.value?.goBack() }
    BackHandler(enabled = isUrlBarEditing) {
        isUrlBarEditing = false
        focusManager.clearFocus()
    }

    Scaffold(
        containerColor = BrowserBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BrowserTopBar(
                currentUrl      = currentTab.url,
                isUrlBarEditing = isUrlBarEditing,
                urlBarValue     = urlBarValue,
                isLoading       = currentTab.isLoading,
                loadingProgress = currentTab.loadProgress,
                isSecure        = currentTab.url.startsWith("https://"),
                canGoBack       = currentTab.canGoBack,
                showMoreMenu    = showMoreMenu,
                focusRequester  = urlFocusRequester,
                onStartEditing  = {
                    urlBarValue = TextFieldValue(
                        text      = currentTab.url,
                        selection = TextRange(0, currentTab.url.length),
                    )
                    isUrlBarEditing = true
                },
                onStopEditing        = { isUrlBarEditing = false; focusManager.clearFocus() },
                onUrlBarValueChanged = { value ->
                    urlBarValue = value
                    viewModel.onUrlBarTextChanged(value.text)
                },
                onNavigate = { text ->
                    focusManager.clearFocus()
                    isUrlBarEditing = false
                    viewModel.onNavigate(text)
                },
                onRefreshStop    = {
                    if (currentTab.isLoading) webViewRef.value?.stopLoading()
                    else webViewRef.value?.reload()
                },
                onBack           = { webViewRef.value?.goBack() },
                onNavigateUp     = onNavigateUp,
                onMoreMenuToggle = { showMoreMenu = !showMoreMenu },
                onMoreMenuDismiss = { showMoreMenu = false },
                onNewTab         = { showMoreMenu = false; onNavigateUp() },
                onMenuBookmarks  = { showMoreMenu = false; onNavigateToBookmarks() },
                onMenuHistory    = { showMoreMenu = false; onNavigateToHistory() },
                onMenuDownloads  = { showMoreMenu = false; onNavigateToDownloads() },
                onMenuSettings   = { showMoreMenu = false; onNavigateToSettings() },
                onShare          = { viewModel.shareCurrentUrl() },
            )
        },
        bottomBar = {
            Column {
                if (!currentTab.isLoading) {
                    BannerAdView(adId = AdManager.getBrowserBannerId())
                }
                AnimatedVisibility(
                    visible = !isImeVisible,
                    enter   = slideInVertically { it },
                    exit    = slideOutVertically { it },
                ) {
                    // Chrome-style bottom bar: back/forward/home/bookmark/share
                    BrowserBottomBar(
                        canGoBack        = currentTab.canGoBack,
                        canGoForward     = currentTab.canGoForward,
                        isBookmarked     = isBookmarked,
                        onBack           = { webViewRef.value?.goBack() },
                        onForward        = { webViewRef.value?.goForward() },
                        onHome           = onNavigateUp,
                        onToggleBookmark = { viewModel.toggleBookmark() },
                        onShare          = { viewModel.shareCurrentUrl() },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (pageError != null && !currentTab.isLoading) {
                BrowserErrorScreen(
                    errorMessage = pageError ?: stringResource(R.string.browser_unknown_error),
                    onTryAgain   = { pageError = null; webViewRef.value?.reload() },
                    onGoHome     = { pageError = null; onNavigateUp() },
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = currentTab.isLoading,
                    onRefresh    = { webViewRef.value?.reload() },
                    state        = pullToRefreshState,
                    modifier     = Modifier.fillMaxSize(),
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled                  = webViewSettings.javaScriptEnabled
                                    domStorageEnabled                  = true
                                    setSupportZoom(true)
                                    builtInZoomControls                = true
                                    displayZoomControls                = false
                                    useWideViewPort                    = true
                                    loadWithOverviewMode               = true
                                    allowFileAccess                    = true
                                    mixedContentMode                   = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                    userAgentString                    = webViewSettings.userAgentString
                                    javaScriptCanOpenWindowsAutomatically = false
                                    loadsImagesAutomatically           = !webViewSettings.dataSaverEnabled
                                    mediaPlaybackRequiresUserGesture   = true
                                }

                                webViewClient = BrowserWebViewClient(
                                    context        = ctx,
                                    onPageStarted  = { url ->
                                        pageError = null
                                        viewModel.onPageStarted(url)
                                        viewModel.onPageNavigated(url)
                                    },
                                    onPageFinished = { url, title ->
                                        viewModel.onPageFinished(url, title, viewModel.currentTab.value.isPrivate)
                                        webViewRef.value?.let { wv ->
                                            viewModel.onNavigationStateChanged(wv.canGoBack(), wv.canGoForward())
                                        }
                                    },
                                    onPageError    = { _, description ->
                                        pageError = description
                                        webViewRef.value?.let { wv ->
                                            viewModel.onNavigationStateChanged(wv.canGoBack(), wv.canGoForward())
                                        }
                                    },
                                    shouldShowAd   = { BuildConfig.ENABLE_ADS },
                                    onAdTrigger    = { viewModel.onAdTrigger() },
                                )

                                webChromeClient = BrowserChromeClient(
                                    onProgressChanged = { progress -> viewModel.onProgressChanged(progress) },
                                    onFaviconReceived = { },
                                    onShowFileChooser = { callback, params ->
                                        fileChooserCallback?.onReceiveValue(null)
                                        fileChooserCallback = callback
                                        try {
                                            fileChooserLauncher.launch(params.createIntent())
                                            true
                                        } catch (e: Exception) {
                                            fileChooserCallback = null
                                            false
                                        }
                                    },
                                )

                                setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                                    val req = DownloadManager.Request(Uri.parse(url)).apply {
                                        setMimeType(mimeType)
                                        setTitle(fileName)
                                        setDescription(context.getString(R.string.browser_download_description))
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                        addRequestHeader("User-Agent", userAgent)
                                        addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
                                    }
                                    fun startDownload() {
                                        (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.browser_download_snackbar, fileName)
                                            )
                                        }
                                    }
                                    if (activity != null) {
                                        AdManager.showBrowserRewarded(
                                            activity   = activity,
                                            onRewarded = { startDownload() },
                                            onDismissed = { startDownload() },
                                        )
                                    } else startDownload()
                                }

                                setBackgroundColor(android.graphics.Color.WHITE)
                                webViewRef.value = this
                                loadUrl(if (initialUrl.isBlank()) "https://www.google.com" else initialUrl)
                            }
                        },
                        update = { wv ->
                            wv.settings.javaScriptEnabled        = webViewSettings.javaScriptEnabled
                            wv.settings.loadsImagesAutomatically = !webViewSettings.dataSaverEnabled
                            val target = currentTab.url
                            if (target.isNotBlank() && target != wv.url) wv.loadUrl(target)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                if (isUrlBarEditing) {
                                    focusManager.clearFocus()
                                    isUrlBarEditing = false
                                }
                            },
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef.value?.apply { stopLoading(); destroy() }
            webViewRef.value = null
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────
// Single compact row: [back] [url bar ──────────] [refresh] [⋮]
// Progress bar underneath, no separate action row

@Composable
private fun BrowserTopBar(
    currentUrl:          String,
    isUrlBarEditing:     Boolean,
    urlBarValue:         TextFieldValue,
    isLoading:           Boolean,
    loadingProgress:     Int,
    isSecure:            Boolean,
    canGoBack:           Boolean,
    showMoreMenu:        Boolean,
    focusRequester:      FocusRequester,
    onStartEditing:      () -> Unit,
    onStopEditing:       () -> Unit,
    onUrlBarValueChanged: (TextFieldValue) -> Unit,
    onNavigate:          (String) -> Unit,
    onRefreshStop:       () -> Unit,
    onBack:              () -> Unit,
    onNavigateUp:        () -> Unit,
    onMoreMenuToggle:    () -> Unit,
    onMoreMenuDismiss:   () -> Unit,
    onNewTab:            () -> Unit,
    onMenuBookmarks:     () -> Unit,
    onMenuHistory:       () -> Unit,
    onMenuDownloads:     () -> Unit,
    onMenuSettings:      () -> Unit,
    onShare:             () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrowserTopBarBg)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Back / close button ──
            IconButton(
                onClick  = { if (canGoBack) onBack() else onNavigateUp() },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector       = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint              = if (canGoBack) IconActive else IconInactive,
                    modifier          = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(4.dp))

            // ── URL bar ──
            Box(modifier = Modifier.weight(1f)) {
                if (isUrlBarEditing) {
                    // Editing state
                    var hasFocused by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(BrowserUrlBarBg)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector       = Icons.Default.Lock,
                            contentDescription = null,
                            tint              = IconInactive,
                            modifier          = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value         = urlBarValue,
                            onValueChange = onUrlBarValueChanged,
                            modifier      = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { fs ->
                                    if (fs.isFocused) hasFocused = true
                                    else if (hasFocused) onStopEditing()
                                },
                            textStyle     = TextStyle(
                                color      = Color.White,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            cursorBrush   = SolidColor(BrowserPurple),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = { onNavigate(urlBarValue.text) }
                            ),
                            singleLine    = true,
                        )
                        if (urlBarValue.text.isNotEmpty()) {
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick           = { onUrlBarValueChanged(TextFieldValue("")) },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector       = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint              = IconInactive,
                                    modifier          = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                } else {
                    // Display state
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(BrowserUrlBarBg)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick           = onStartEditing,
                            )
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector       = if (isSecure || currentUrl.isBlank())
                                Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = null,
                            tint              = when {
                                currentUrl.isBlank() -> IconDisabled
                                isSecure             -> SwiftGreen
                                else                 -> SwiftRed
                            },
                            modifier          = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text      = if (currentUrl.isBlank())
                                stringResource(R.string.url_bar_hint)
                            else currentUrl.toDisplayUrl(),
                            color     = if (currentUrl.isBlank()) IconInactive else Color.White,
                            fontSize  = 14.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // ── Refresh / Stop ──
            IconButton(
                onClick  = onRefreshStop,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector       = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                    contentDescription = if (isLoading) stringResource(R.string.action_stop)
                    else stringResource(R.string.action_reload),
                    tint              = IconInactive,
                    modifier          = Modifier.size(20.dp),
                )
            }

            // ── Three-dot menu ──
            Box {
                IconButton(
                    onClick  = onMoreMenuToggle,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector       = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint              = IconInactive,
                        modifier          = Modifier.size(20.dp),
                    )
                }
                DropdownMenu(
                    expanded         = showMoreMenu,
                    onDismissRequest = onMoreMenuDismiss,
                    containerColor   = BrowserMenuBg,
                    shape            = RoundedCornerShape(14.dp),
                    tonalElevation   = 0.dp,
                    shadowElevation  = 12.dp,
                ) {
                    MenuTextItem("Home",      onNewTab)
                    MenuTextItem("Bookmarks", onMenuBookmarks)
                    MenuTextItem("History",   onMenuHistory)
                    MenuTextItem("Downloads", onMenuDownloads)
                    MenuTextItem("Share") { onMoreMenuDismiss(); onShare() }
                    MenuTextItem("Settings",  onMenuSettings)
                }
            }
        }

        // ── Progress bar ──
        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(BrowserUrlBarBg),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(loadingProgress / 100f)
                        .fillMaxHeight()
                        .background(BrowserProgressColor),
                )
            }
        }
    }
}

@Composable
private fun MenuTextItem(label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text     = label,
                color    = Color.White,
                fontSize = 14.sp,
            )
        },
        onClick = onClick,
    )
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────
// Chrome-style: back | forward | home | bookmark | share
// All 5 actions in a single clean row, no labels, just icons

@Composable
private fun BrowserBottomBar(
    canGoBack:        Boolean,
    canGoForward:     Boolean,
    isBookmarked:     Boolean,
    onBack:           () -> Unit,
    onForward:        () -> Unit,
    onHome:           () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare:          () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(BrowserDivider)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrowserBottomBg)
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            BottomBarBtn(
                onClick = onBack,
                enabled = canGoBack,
            ) {
                Icon(
                    imageVector       = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint              = if (canGoBack) IconActive else IconDisabled,
                    modifier          = Modifier.size(24.dp),
                )
            }
            BottomBarBtn(onClick = onForward, enabled = canGoForward) {
                Icon(
                    imageVector       = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.action_forward),
                    tint              = if (canGoForward) IconActive else IconDisabled,
                    modifier          = Modifier.size(24.dp),
                )
            }
            BottomBarBtn(onClick = onHome, enabled = true) {
                Icon(
                    imageVector       = Icons.Default.Home,
                    contentDescription = "Home",
                    tint              = IconInactive,
                    modifier          = Modifier.size(24.dp),
                )
            }
            BottomBarBtn(onClick = onToggleBookmark, enabled = true) {
                Icon(
                    imageVector       = if (isBookmarked) Icons.Filled.Bookmark
                    else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(R.string.action_bookmark),
                    tint              = if (isBookmarked) BrowserPurple else IconInactive,
                    modifier          = Modifier.size(24.dp),
                )
            }
            BottomBarBtn(onClick = onShare, enabled = true) {
                Icon(
                    imageVector       = Icons.Default.Share,
                    contentDescription = stringResource(R.string.action_share),
                    tint              = IconInactive,
                    modifier          = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun BottomBarBtn(
    onClick:  () -> Unit,
    enabled:  Boolean,
    content:  @Composable () -> Unit,
) {
    IconButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier.size(52.dp),
    ) {
        content()
    }
}

// ─── Error Screen ─────────────────────────────────────────────────────────────

@Composable
private fun BrowserErrorScreen(
    errorMessage: String,
    onTryAgain:   () -> Unit,
    onGoHome:     () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrowserBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector       = Icons.Default.WifiOff,
            contentDescription = null,
            modifier          = Modifier.size(64.dp),
            tint              = IconDisabled,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text       = stringResource(R.string.browser_error_title),
            color      = Color.White.copy(alpha = 0.85f),
            fontSize   = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign  = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = errorMessage,
            color     = IconInactive,
            fontSize  = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick        = onTryAgain,
            colors         = ButtonDefaults.buttonColors(containerColor = BrowserPurple),
            shape          = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
        ) {
            Text(
                text       = stringResource(R.string.browser_try_again),
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onGoHome,
            border  = BorderStroke(1.dp, BrowserDivider),
            shape   = RoundedCornerShape(24.dp),
            colors  = ButtonDefaults.outlinedButtonColors(contentColor = IconInactive),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
        ) {
            Text(
                text     = stringResource(R.string.browser_go_home),
                fontSize = 15.sp,
            )
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
private fun BrowserTopBarPreview() {
    SwiftBrowserTheme {
        BrowserTopBar(
            currentUrl           = "https://www.google.com",
            isUrlBarEditing      = false,
            urlBarValue          = TextFieldValue(""),
            isLoading            = false,
            loadingProgress      = 0,
            isSecure             = true,
            canGoBack            = true,
            showMoreMenu         = false,
            focusRequester       = remember { FocusRequester() },
            onStartEditing       = {},
            onStopEditing        = {},
            onUrlBarValueChanged = {},
            onNavigate           = {},
            onRefreshStop        = {},
            onBack               = {},
            onNavigateUp         = {},
            onMoreMenuToggle     = {},
            onMoreMenuDismiss    = {},
            onNewTab             = {},
            onMenuBookmarks      = {},
            onMenuHistory        = {},
            onMenuDownloads      = {},
            onMenuSettings       = {},
            onShare              = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
private fun BrowserBottomBarPreview() {
    SwiftBrowserTheme {
        BrowserBottomBar(
            canGoBack        = true,
            canGoForward     = false,
            isBookmarked     = true,
            onBack           = {},
            onForward        = {},
            onHome           = {},
            onToggleBookmark = {},
            onShare          = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
private fun BrowserErrorScreenPreview() {
    SwiftBrowserTheme {
        BrowserErrorScreen(
            errorMessage = "net::ERR_NAME_NOT_RESOLVED",
            onTryAgain   = {},
            onGoHome     = {},
        )
    }
}