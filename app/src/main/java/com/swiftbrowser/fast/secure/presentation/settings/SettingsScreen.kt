package com.swiftbrowser.fast.secure.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.AdManager
import com.swiftbrowser.fast.secure.core.ads.BannerAdView
import com.swiftbrowser.fast.secure.presentation.navigation.Screen

private val BgColor = Color(0xFF0A0010)
private val AccentPurple = Color(0xFF6C47D9)
private val CardColor = Color(0xFF1C1C1E)

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgColor,
        bottomBar = {
            Column {
                BannerAdView(adId = AdManager.getSettingsBannerId())
                SettingsNavBar(navController)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            AppLogoSection()
            Spacer(Modifier.height(28.dp))
            SettingsCards(
                onDownloadInstructionsClick = {
                    navController.navigate(Screen.Browser.createRoute("https://swiftbrowser.app/instructions"))
                },
                onPrivacyPolicyClick = {
                    navController.navigate(Screen.Browser.createRoute("https://sites.google.com/view/swiftbrowseraibrowser/home"))
                },
                onRateUsClick = { openPlayStore(context) }
            )
            Spacer(Modifier.height(16.dp))
            AppVersionPill()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AppLogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, BgColor, CircleShape)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Swift Browser",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.settings_app_tagline),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingsCards(
    onDownloadInstructionsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onRateUsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardColor)
                    .clickable(onClick = onDownloadInstructionsClick)
                    .padding(14.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_download_instructions),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_download_instructions),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardColor)
                    .clickable(onClick = onPrivacyPolicyClick)
                    .padding(14.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_privacy_policy),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.settings_privacy_policy),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardColor)
                .clickable(onClick = onRateUsClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_rate_us),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.settings_rate_us),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AppVersionPill() {
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }
        catch (e: Exception) { "" }
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_version_label, versionName),
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun SettingsNavBar(navController: NavController) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .navigationBarsPadding()
        ) {
            SettingsNavTab(
                activeIconRes = R.drawable.ic_nav_home_active,
                inactiveIconRes = R.drawable.ic_nav_home,
                label = stringResource(R.string.nav_home),
                isActive = false,
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
            SettingsNavTab(
                activeIconRes = R.drawable.ic_nav_search_active,
                inactiveIconRes = R.drawable.ic_nav_search,
                label = stringResource(R.string.url_bar_search),
                isActive = false,
                onClick = {
                    navController.navigate(Screen.Browser.createRoute(""))
                }
            )
            SettingsNavTab(
                activeIconRes = R.drawable.ic_nav_downloads_active,
                inactiveIconRes = R.drawable.ic_nav_downloads,
                label = stringResource(R.string.nav_downloads),
                isActive = false,
                onClick = {
                    navController.navigate(Screen.Downloads.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            SettingsNavTab(
                activeIconRes = R.drawable.ic_nav_settings_active,
                inactiveIconRes = R.drawable.ic_nav_settings,
                label = stringResource(R.string.nav_settings),
                isActive = true,
                onClick = {}
            )
        }
    }
}

@Composable
private fun RowScope.SettingsNavTab(
    activeIconRes: Int,
    inactiveIconRes: Int,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        color = AccentPurple,
                        radius = 3.dp.toPx(),
                        center = Offset(size.width / 2f, 3.dp.toPx())
                    )
                }
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(if (isActive) activeIconRes else inactiveIconRes),
            contentDescription = label,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun openPlayStore(context: Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
        )
    } catch (e: Exception) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
        )
    }
}
