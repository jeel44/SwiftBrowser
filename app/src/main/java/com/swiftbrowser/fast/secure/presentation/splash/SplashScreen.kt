package com.swiftbrowser.fast.secure.presentation.splash

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.swiftbrowser.fast.secure.R
import com.google.android.gms.ads.MobileAds
import com.swiftbrowser.fast.secure.core.ads.AppOpenAdManager
import com.swiftbrowser.fast.secure.core.ads.OnboardingAdManager
import com.swiftbrowser.fast.secure.core.ads.RemoteConfigManager
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import com.swiftbrowser.fast.secure.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    var showNotificationDialog by remember { mutableStateOf(false) }
    var navigateTarget by remember { mutableStateOf("") }

    fun navigateWithAd(target: String) {
        AppOpenAdManager.showSplashAppOpenAd(activity) {
            navController.navigate(target) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = {
                if (navigateTarget.isNotEmpty()) {
                    navigateWithAd(navigateTarget)
                }
            }
        )
    } else null

    LaunchedEffect(Unit) {
        // Step 1: Fetch Remote Config (5s max)
        withTimeoutOrNull(5000L) {
            RemoteConfigManager.fetchAndActivate()
        }

        // Step 2: Init MobileAds SDK
        MobileAds.initialize(context) {}

        // Step 3: Preload background app open ad for future use
        AppOpenAdManager.preloadAll(context)

        // Step 4: Preload onboarding ads in background (ready by the time user reaches onboarding)
        OnboardingAdManager.loadNativeAd(context)
        OnboardingAdManager.loadInterstitialAd(context)

        // Step 5: Small wait
        delay(1000L)

        // Step 6: Determine navigation target
        val prefs = BrowserPreferences(context.applicationContext)
        @Suppress("DEPRECATION")
        val currentVersion = context.packageManager
            .getPackageInfo(context.packageName, 0).versionCode
        val savedVersionCode = prefs.appVersionCode.first()
        val isFirst = prefs.isFirstLaunch.first()
        val target = if (savedVersionCode != currentVersion) {
            prefs.resetFirstLaunch()
            prefs.setAppVersionCode(currentVersion)
            Screen.Language.route
        } else if (!isFirst) {
            Screen.Home.route
        } else {
            Screen.Language.route
        }

        // Step 7: Handle notification permission, then show ad + navigate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                navigateWithAd(target)
            } else {
                navigateTarget = target
                showNotificationDialog = true
            }
        } else {
            navigateWithAd(target)
        }
    }

    var progressTarget by remember { mutableFloatStateOf(0f) }
    val loadingProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "loader"
    )
    LaunchedEffect(Unit) {
        progressTarget = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0010)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { com.swiftbrowser.fast.secure.core.ui.StarfieldView(it) },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF6C47D9),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(Color(0xFF6C47D9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Swift Browser",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA78BFA),
                letterSpacing = 2.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(3.dp)
                    .background(Color(0xFF2A1F4E), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loadingProgress)
                        .background(Color(0xFF6C47D9), RoundedCornerShape(2.dp))
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.splash_ads_disclaimer),
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.25f),
                textAlign = TextAlign.Center
            )
        }

        if (showNotificationDialog) {
            SplashNotificationPermissionDialog(
                onAllow = {
                    showNotificationDialog = false
                    scope.launch {
                        BrowserPreferences(context.applicationContext)
                            .setNotificationPermissionAsked(true)
                    }
                    notificationPermissionState?.launchPermissionRequest()
                },
                onNotNow = {
                    showNotificationDialog = false
                    scope.launch {
                        BrowserPreferences(context.applicationContext)
                            .setNotificationPermissionAsked(true)
                    }
                    navigateWithAd(navigateTarget)
                }
            )
        }
    }
}

@Composable
private fun SplashNotificationPermissionDialog(
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0010).copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1C1C1E))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF6C47D9).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF6C47D9),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.splash_notif_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_notif_body),
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47D9)),
                shape = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.splash_notif_allow),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.splash_notif_not_now),
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
