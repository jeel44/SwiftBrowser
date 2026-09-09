package com.swiftbrowser.fast.secure.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.presentation.navigation.Screen
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBlue
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBorder
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBorderStrong
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme
import com.swiftbrowser.fast.secure.presentation.theme.SwiftCyan
import com.swiftbrowser.fast.secure.presentation.theme.SwiftNavyDark
import com.swiftbrowser.fast.secure.presentation.theme.SwiftSurface2
import com.swiftbrowser.fast.secure.presentation.theme.SwiftTextPrimary
import com.swiftbrowser.fast.secure.presentation.theme.SwiftTextSecondary

// ─────────────────────────────── Bottom Nav ───────────────────────────────

@Composable
fun SwiftBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    Column {
        HorizontalDivider(color = SwiftBorder, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SwiftNavyDark),
        ) {
            BottomNavTabItem(
                filledIcon = Icons.Filled.Home,
                outlinedIcon = Icons.Outlined.Home,
                label = "Home",
                isSelected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) },
            )
            BottomNavTabItem(
                filledIcon = Icons.Filled.Search,
                outlinedIcon = Icons.Outlined.Search,
                label = "Search",
                isSelected = currentRoute == Screen.Browser.route,
                onClick = { onNavigate(Screen.Browser.createRoute("")) },
            )
            BottomNavTabItem(
                filledIcon = Icons.Filled.Download,
                outlinedIcon = Icons.Outlined.Download,
                label = "Downloads",
                isSelected = currentRoute == Screen.Downloads.route,
                onClick = { onNavigate(Screen.Downloads.route) },
            )
            BottomNavTabItem(
                filledIcon = Icons.Filled.Settings,
                outlinedIcon = Icons.Outlined.Settings,
                label = "Settings",
                isSelected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) },
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavTabItem(
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = SwiftBlue
    val inactiveColor = SwiftTextPrimary.copy(alpha = 0.35f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .drawBehind {
                if (isSelected) {
                    drawLine(
                        color = activeColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 3.dp.toPx(),
                    )
                }
            }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isSelected) filledIcon else outlinedIcon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.W500 else FontWeight.Normal,
        )
    }
}

// ─────────────────────────────── Section Header ───────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    actionText: String = "",
    onAction: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = SwiftTextPrimary.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.W500,
        )
        if (actionText.isNotEmpty()) {
            Text(
                text = actionText,
                color = SwiftBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

// ─────────────────────────────── Settings Row ───────────────────────────────

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBgColor: Color,
    label: String,
    value: String = "",
    showToggle: Boolean = false,
    toggleState: Boolean = false,
    onToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = SwiftTextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotEmpty() && !showToggle) {
            Text(
                text = value,
                color = SwiftTextSecondary,
                fontSize = 14.sp,
            )
        }
        if (showToggle) {
            Switch(
                checked = toggleState,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SwiftBlue,
                    uncheckedThumbColor = SwiftTextSecondary,
                    uncheckedTrackColor = SwiftSurface2,
                ),
            )
        }
    }
}

// ─────────────────────────────── Native Ad Card ───────────────────────────────

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(Unit) {
        AdLoader.Builder(context, Constants.AdUnitIds.NATIVE)
            .forNativeAd { ad -> nativeAd = ad }
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose { nativeAd?.destroy() }
    }

    val adShape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(adShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SwiftBlue.copy(alpha = 0.15f),
                        SwiftCyan.copy(alpha = 0.10f),
                    )
                )
            )
            .border(0.5.dp, SwiftBlue.copy(alpha = 0.30f), adShape),
    ) {
        val ad = nativeAd
        if (ad != null) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    NativeAdView(ctx).also { adView ->
                        val headlineView = android.widget.TextView(ctx).apply {
                            setTextColor(SwiftTextPrimary.toArgb())
                            textSize = 14f
                            setTypeface(typeface, android.graphics.Typeface.BOLD)
                        }
                        val bodyView = android.widget.TextView(ctx).apply {
                            setTextColor(SwiftTextSecondary.toArgb())
                            textSize = 12f
                        }
                        val ctaButton = android.widget.Button(ctx).apply {
                            setTextColor(android.graphics.Color.WHITE)
                            setBackgroundColor(SwiftBlue.toArgb())
                        }
                        val pad = (12 * ctx.resources.displayMetrics.density).toInt()
                        android.widget.LinearLayout(ctx).apply {
                            orientation = android.widget.LinearLayout.VERTICAL
                            setPadding(pad, pad, pad, pad)
                            addView(headlineView)
                            addView(bodyView)
                            addView(ctaButton)
                        }.also { adView.addView(it) }
                        adView.headlineView = headlineView
                        adView.bodyView = bodyView
                        adView.callToActionView = ctaButton
                    }
                },
                update = { adView ->
                    (adView.headlineView as android.widget.TextView).text = ad.headline
                    (adView.bodyView as android.widget.TextView).text = ad.body
                    (adView.callToActionView as android.widget.Button).text = ad.callToAction
                    adView.setNativeAd(ad)
                },
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SwiftBlue,
                ) {
                    Text(
                        text = "Ad",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sponsored",
                    color = SwiftTextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

// ─────────────────────────────── Previews ───────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SwiftBottomNavPreview() {
    SwiftBrowserTheme {
        SwiftBottomNav(currentRoute = Screen.Home.route, onNavigate = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    SwiftBrowserTheme {
        SectionHeader(title = "Speed Dial", actionText = "Edit")
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsRowPreview() {
    SwiftBrowserTheme {
        SettingsRow(
            icon = Icons.Filled.Settings,
            iconBgColor = SwiftBlue,
            label = "Dark Mode",
            showToggle = true,
            toggleState = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NativeAdCardPreview() {
    SwiftBrowserTheme {
        NativeAdCard(modifier = Modifier.padding(16.dp))
    }
}
