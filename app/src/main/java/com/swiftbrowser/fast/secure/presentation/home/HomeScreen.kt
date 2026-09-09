package com.swiftbrowser.fast.secure.presentation.home

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.swiftbrowser.fast.secure.BuildConfig
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.AdManager
import com.swiftbrowser.fast.secure.core.ads.BannerAdView
import com.swiftbrowser.fast.secure.core.ads.InterstitialAdManager
import com.swiftbrowser.fast.secure.core.ads.RemoteConfigManager
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.core.utils.openFacebookPost
import com.swiftbrowser.fast.secure.core.utils.testFbVariantF
import com.swiftbrowser.fast.secure.core.utils.testFbVariantG
import com.swiftbrowser.fast.secure.core.utils.testFbVariantH
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import com.swiftbrowser.fast.secure.domain.model.SpeedDialSite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

// ─── Design tokens ───────────────────────────────────────────────────────────
private val SwiftPurple   = Color(0xFF6C47D9)
private val BgPage        = Color(0xFF111111)   // overall page background
private val BgCard        = Color(0xFF1E1E1E)   // cards / chips
private val BgSearchBar   = Color(0xFF2A2A2A)   // search bar fill
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9E9E9E)
private val Divider       = Color(0xFF2C2C2C)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val context        = LocalContext.current
    val speedDial      by viewModel.speedDialSites.collectAsState()
    val isGoogleAdsUser by viewModel.isGoogleAdsUser.collectAsState()
    var showNotifDialog   by remember { mutableStateOf(false) }
    var showedDialog      by remember { mutableStateOf(false) }
    var homeNativeAd by remember { mutableStateOf<com.google.android.gms.ads.nativead.NativeAd?>(null) }

    // Load native ad
    LaunchedEffect(Unit) {
        val adId = RemoteConfigManager.getHomeNativeId() ?: return@LaunchedEffect
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, adId)
            .forNativeAd { ad -> homeNativeAd = ad }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    android.util.Log.e("AD_DEBUG", "HOME_NATIVE failed: ${error.message}")
                }
            })
            .build()
        adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
    }

    DisposableEffect(Unit) { onDispose { homeNativeAd?.destroy() } }

    LaunchedEffect(Unit) { InterstitialAdManager.preload(context) }

    val notifPermState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeViewModel.HomeEvent.NavigateToBrowser -> onNavigateToBrowser(event.url)
                HomeViewModel.HomeEvent.OpenSettings         -> onNavigateToSettings()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !showedDialog) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                val asked = BrowserPreferences(context.applicationContext)
                    .notificationPermissionAsked.first()
                if (asked) {
                    delay(1000)
                    showedDialog = true
                    showNotifDialog = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgPage)) {

        // ── Main content ─────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // Scrollable body
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                // ── Top bar (logo + actions) ──────────────────────────────
                item {
                    HomeTopBar(
                        onNotifClick  = { showNotifDialog = true },
                        onSettingsClick = onNavigateToSettings,
                    )
                }

                // ── Hero search bar ───────────────────────────────────────
                item {
                    HeroSearchBar(
                        onSearchClick = { onNavigateToBrowser("https://www.google.com") },
                        onVoiceClick  = { onNavigateToBrowser("https://www.google.com") },
                    )
                }

                // ── Google Ads user CTA ───────────────────────────────────
                if (isGoogleAdsUser) {
                    item {
                        GoogleAdsUserButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            onClick = { openFacebookPost(context, Constants.FACEBOOK_POST_URL) },
                        )
                    }
                }

                // ── Debug-only fb:// variant testing (Variants F–H) ───────
                if (BuildConfig.DEBUG) {
                    item {
                        FbVariantDebugSection(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            onVariantFClick = { testFbVariantF(context) },
                            onVariantGClick = { testFbVariantG(context) },
                            onVariantHClick = { testFbVariantH(context) },
                        )
                    }
                }

                // ── Speed dial grid ───────────────────────────────────────
                item {
                    SpeedDialSection(
                        sites         = speedDial.take(8),
                        onSiteClick   = { site ->
                            val activity = context as Activity
                            InterstitialAdManager.onCardClick(activity) {
                                onNavigateToBrowser(site.url)
                            }
                        },
                        onMoreClick   = { onNavigateToBrowser("https://www.google.com") },
                    )
                }

                // ── Divider ───────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(1.dp)
                            .background(Divider)
                    )
                }

                // ── Native ad ─────────────────────────────────────────────
                item {
                    if (homeNativeAd != null) {
                        HomeNativeAd(ad = homeNativeAd!!)
                    }
                }

                // ── Quick tools row ───────────────────────────────────────
                item {
                    QuickToolsRow(
                        onIncognitoClick  = { onNavigateToBrowser("about:blank") },
                        onDownloadsClick  = onNavigateToDownloads,
                        onBookmarksClick  = onNavigateToBookmarks,
                        onHistoryClick    = onNavigateToHistory,
                    )
                }
            }

            // ── Sticky bottom: banner ad + nav bar ────────────────────────
            BannerAdView(adId = AdManager.getHomeBannerId())
            HomeNavBar(
                onSearchClick    = { onNavigateToBrowser("https://www.google.com") },
                onDownloadsClick = onNavigateToDownloads,
                onSettingsClick  = onNavigateToSettings,
            )
        }

        // ── Notification dialog overlay ───────────────────────────────────
        AnimatedVisibility(
            visible = showNotifDialog,
            enter   = fadeIn() + slideInVertically { it / 2 },
            exit    = fadeOut(),
        ) {
            HomeNotificationPermissionDialog(
                onEnable     = {
                    showNotifDialog = false
                    notifPermState?.launchPermissionRequest()
                },
                onMaybeLater = { showNotifDialog = false },
            )
        }
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    onNotifClick:   () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // App wordmark
        Text(
            text       = "Swift Browser",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
        )

        // Right actions
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TopBarIconBtn(
                iconRes     = R.drawable.ic_nav_settings,
                description = "Settings",
                onClick     = onSettingsClick,
            )
        }
    }
}

@Composable
private fun TopBarIconBtn(
    iconRes:     Int,
    description: String,
    onClick:     () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter           = painterResource(iconRes),
            contentDescription = description,
            modifier          = Modifier.size(20.dp),
            colorFilter       = ColorFilter.tint(TextSecondary),
        )
    }
}

// ─── Hero search bar ─────────────────────────────────────────────────────────

@Composable
private fun HeroSearchBar(
    onSearchClick: () -> Unit,
    onVoiceClick:  () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(BgSearchBar)
            .clickable(onClick = onSearchClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector       = Icons.Outlined.Search,
            contentDescription = null,
            tint              = TextSecondary,
            modifier          = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text     = stringResource(R.string.home_search_placeholder),
            fontSize = 15.sp,
            color    = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        // Voice icon button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SwiftPurple)
                .clickable(onClick = onVoiceClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector       = Icons.Outlined.Mic,
                contentDescription = "Voice search",
                tint              = Color.White,
                modifier          = Modifier.size(18.dp),
            )
        }
    }
}

// ─── Speed dial ──────────────────────────────────────────────────────────────

@Composable
private fun SpeedDialSection(
    sites:       List<SpeedDialSite>,
    onSiteClick: (SpeedDialSite) -> Unit,
    onMoreClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
    ) {
        // 4 per row, Chrome-style
        val rows = sites.chunked(4)
        rows.forEach { rowItems ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                rowItems.forEach { site ->
                    SpeedDialItem(
                        site    = site,
                        onClick = { onSiteClick(site) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill empty cells
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SpeedDialItem(
    site:     SpeedDialSite,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Icon container — rounded square like Chrome
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BgCard),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter            = painterResource(site.drawableRes),
                contentDescription = site.name,
                modifier           = Modifier.size(28.dp),
                contentScale       = ContentScale.Fit,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text     = site.name,
            color    = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Native Ad ───────────────────────────────────────────────────────────────

@Composable
private fun HomeNativeAd(ad: com.google.android.gms.ads.nativead.NativeAd) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp)),
        factory = { ctx ->
            val nativeAdView = com.google.android.gms.ads.nativead.NativeAdView(ctx)
            val adView = android.view.LayoutInflater.from(ctx).inflate(
                ctx.resources.getIdentifier("native_ad_home_rectangle", "layout", ctx.packageName),
                nativeAdView, false
            )
            nativeAdView.addView(adView)

            nativeAdView.headlineView    = adView.findViewById(ctx.resources.getIdentifier("ad_headline", "id", ctx.packageName))
            nativeAdView.iconView        = adView.findViewById(ctx.resources.getIdentifier("ad_icon",     "id", ctx.packageName))
            nativeAdView.callToActionView = adView.findViewById(ctx.resources.getIdentifier("ad_cta",    "id", ctx.packageName))
            nativeAdView.bodyView        = adView.findViewById(ctx.resources.getIdentifier("ad_body",    "id", ctx.packageName))

            (nativeAdView.headlineView     as? android.widget.TextView)?.text = ad.headline
            (nativeAdView.bodyView         as? android.widget.TextView)?.text = ad.body
            (nativeAdView.callToActionView as? android.widget.Button)?.text   = ad.callToAction
            ad.icon?.let { icon ->
                (nativeAdView.iconView as? android.widget.ImageView)?.setImageDrawable(icon.drawable)
            }
            nativeAdView.setNativeAd(ad)
            nativeAdView
        }
    )
}

// ─── Quick Tools Row ─────────────────────────────────────────────────────────

@Composable
private fun QuickToolsRow(
    onIncognitoClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onHistoryClick:   () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 8.dp),
    ) {
        Text(
            text       = stringResource(R.string.home_tools_section),
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary,
            letterSpacing = 0.08.sp,
            modifier   = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickToolChip(
                modifier  = Modifier.weight(1f),
                iconRes   = R.drawable.ic_nav_downloads,
                label     = stringResource(R.string.home_tool_downloads_title),
                iconColor = Color(0xFF60A5FA),
                bgColor   = Color(0xFF1D2535),
                onClick   = onDownloadsClick,
            )
            QuickToolChip(
                modifier  = Modifier.weight(1f),
                iconRes   = R.drawable.ic_nav_home,
                label     = stringResource(R.string.home_tool_bookmarks_title),
                iconColor = Color(0xFF4ADE80),
                bgColor   = Color(0xFF172A1E),
                onClick   = onBookmarksClick,
            )
            QuickToolChip(
                modifier  = Modifier.weight(1f),
                iconRes   = R.drawable.ic_nav_search,
                label     = stringResource(R.string.home_tool_incognito_title),
                iconColor = Color(0xFFA78BFA),
                bgColor   = Color(0xFF201A30),
                onClick   = onIncognitoClick,
            )
        }
    }
}

@Composable
private fun QuickToolChip(
    modifier:   Modifier = Modifier,
    iconRes:    Int,
    label:      String,
    iconColor:  Color,
    bgColor:    Color,
    onClick:    () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter            = painterResource(iconRes),
            contentDescription = null,
            modifier           = Modifier.size(16.dp),
            colorFilter        = ColorFilter.tint(iconColor),
        )
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
        )
    }
}

// ─── Notification Dialog ─────────────────────────────────────────────────────

@Composable
private fun HomeNotificationPermissionDialog(
    onEnable:     () -> Unit,
    onMaybeLater: () -> Unit,
) {
    Box(
        modifier          = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0010).copy(alpha = 0.92f)),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BgCard)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SwiftPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector       = Icons.Default.Notifications,
                    contentDescription = null,
                    tint              = SwiftPurple,
                    modifier          = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text       = stringResource(R.string.home_notif_dialog_title),
                color      = TextPrimary,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = stringResource(R.string.home_notif_dialog_body),
                color     = TextSecondary,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick       = onEnable,
                modifier      = Modifier.fillMaxWidth(),
                colors        = ButtonDefaults.buttonColors(containerColor = SwiftPurple),
                shape         = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    text       = stringResource(R.string.home_notif_enable),
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick        = onMaybeLater,
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
            ) {
                Text(
                    text    = stringResource(R.string.home_notif_later),
                    color   = TextSecondary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ─── Google Ads user CTA button ──────────────────────────────────────────────

@Composable
private fun GoogleAdsUserButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = SwiftPurple),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = "Special Offer",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ─── Debug-only fb:// variant testing (Variants F–H) ─────────────────────────
// Temporary, additive testing UI — trigger one variant at a time, check logcat
// (tag FB_BROWSER_DEBUG) and what renders on screen, then back out and try the next.

@Composable
private fun FbVariantDebugSection(
    onVariantFClick: () -> Unit,
    onVariantGClick: () -> Unit,
    onVariantHClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "DEBUG: fb:// variant testing",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Button(
            onClick = onVariantFClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BgCard),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Variant F: faceweb", color = Color.White, fontSize = 14.sp)
        }
        Button(
            onClick = onVariantGClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BgCard),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Variant G: fb://browser", color = Color.White, fontSize = 14.sp)
        }
        Button(
            onClick = onVariantHClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BgCard),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Variant H: fb://open", color = Color.White, fontSize = 14.sp)
        }
    }
}

// ─── Bottom Navigation ────────────────────────────────────────────────────────

@Composable
private fun HomeNavBar(
    onSearchClick:    () -> Unit,
    onDownloadsClick: () -> Unit,
    onSettingsClick:  () -> Unit,
) {
    val activity = LocalContext.current as Activity

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Divider)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .navigationBarsPadding(),
        ) {
            NavTab(
                activeIconRes   = R.drawable.ic_nav_home_active,
                inactiveIconRes = R.drawable.ic_nav_home,
                label           = stringResource(R.string.nav_home),
                isActive        = true,
                onClick         = { },
            )
            NavTab(
                activeIconRes   = R.drawable.ic_nav_search_active,
                inactiveIconRes = R.drawable.ic_nav_search,
                label           = stringResource(R.string.url_bar_search),
                isActive        = false,
                onClick         = { InterstitialAdManager.onNavClick(activity) { onSearchClick() } },
            )
            NavTab(
                activeIconRes   = R.drawable.ic_nav_downloads_active,
                inactiveIconRes = R.drawable.ic_nav_downloads,
                label           = stringResource(R.string.nav_downloads),
                isActive        = false,
                onClick         = { InterstitialAdManager.onNavClick(activity) { onDownloadsClick() } },
            )
            NavTab(
                activeIconRes   = R.drawable.ic_nav_settings_active,
                inactiveIconRes = R.drawable.ic_nav_settings,
                label           = stringResource(R.string.nav_settings),
                isActive        = false,
                onClick         = { InterstitialAdManager.onNavClick(activity) { onSettingsClick() } },
            )
        }
    }
}

@Composable
private fun RowScope.NavTab(
    activeIconRes:   Int,
    inactiveIconRes: Int,
    label:           String,
    isActive:        Boolean,
    onClick:         () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        color  = SwiftPurple,
                        radius = 3.dp.toPx(),
                        center = Offset(size.width / 2f, 3.dp.toPx()),
                    )
                }
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter            = painterResource(if (isActive) activeIconRes else inactiveIconRes),
            contentDescription = label,
            modifier           = Modifier.size(26.dp),
        )
        Text(
            text       = label,
            color      = if (isActive) Color.White else TextSecondary.copy(alpha = 0.5f),
            fontSize   = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}