package com.swiftbrowser.fast.secure.presentation.onboarding

import android.app.Activity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.ads.nativead.NativeAd
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.NativeAdOverlay
import com.swiftbrowser.fast.secure.core.ads.OnboardingAdManager
import com.swiftbrowser.fast.secure.core.ads.RemoteConfigManager
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import com.swiftbrowser.fast.secure.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Colors ───────────────────────────────────────────────────────────────────

private val BgDark = Color(0xFF0A0010)
private val Purple = Color(0xFF6C47D9)
private val PurpleLight = Color(0xFFa78bfa)
private val BottomSheetBg = Color(0xFF111118)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as Activity

    var showNativeAdOverlay by remember { mutableStateOf(false) }
    var currentNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var lastSeenPage by remember { mutableIntStateOf(0) }
    LaunchedEffect(pagerState.currentPage) {
        val newPage = pagerState.currentPage
        if (newPage > lastSeenPage) {
            if (newPage == 1 || newPage == 2) {
                val adId = RemoteConfigManager.getOnboardingNativeId()
                if (adId != null) {
                    showNativeAdOverlay = true
                }
            }
        }
        lastSeenPage = newPage
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { pageIndex ->
            val isCurrentPage = pagerState.currentPage == pageIndex

            var illustrationVisible by remember { mutableStateOf(false) }
            var chip1Visible by remember { mutableStateOf(false) }
            var chip2Visible by remember { mutableStateOf(false) }
            var chip3Visible by remember { mutableStateOf(false) }
            var bottomVisible by remember { mutableStateOf(false) }

            LaunchedEffect(isCurrentPage) {
                if (isCurrentPage) {
                    illustrationVisible = false
                    chip1Visible = false
                    chip2Visible = false
                    chip3Visible = false
                    bottomVisible = false
                    delay(16)
                    illustrationVisible = true
                    chip1Visible = true
                    delay(80)
                    chip2Visible = true
                    delay(80)
                    chip3Visible = true
                    delay(80)
                    bottomVisible = true
                }
            }

            val illustrationScale by animateFloatAsState(
                targetValue = if (illustrationVisible) 1f else 0.85f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                label = "illustrationScale"
            )

            fun chipSpec() = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)

            val chip1Y by animateFloatAsState(targetValue = if (chip1Visible) 0f else 60f, animationSpec = chipSpec(), label = "c1y")
            val chip1A by animateFloatAsState(targetValue = if (chip1Visible) 1f else 0f, animationSpec = chipSpec(), label = "c1a")
            val chip2Y by animateFloatAsState(targetValue = if (chip2Visible) 0f else 60f, animationSpec = chipSpec(), label = "c2y")
            val chip2A by animateFloatAsState(targetValue = if (chip2Visible) 1f else 0f, animationSpec = chipSpec(), label = "c2a")
            val chip3Y by animateFloatAsState(targetValue = if (chip3Visible) 0f else 60f, animationSpec = chipSpec(), label = "c3y")
            val chip3A by animateFloatAsState(targetValue = if (chip3Visible) 1f else 0f, animationSpec = chipSpec(), label = "c3a")

            val bottomY by animateFloatAsState(
                targetValue = if (bottomVisible) 0f else 100f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "bottomY"
            )
            val bottomAlpha by animateFloatAsState(
                targetValue = if (bottomVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "bottomAlpha"
            )

            val infiniteTransition = rememberInfiniteTransition(label = "glow")
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glowScale"
            )

            val chipAnims = listOf(
                chip1Y to chip1A,
                chip2Y to chip2A,
                chip3Y to chip3A
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // ── Illustration area ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { scaleX = illustrationScale; scaleY = illustrationScale },
                    contentAlignment = Alignment.Center
                ) {
                    when (pageIndex) {
                        0 -> Page1Illustration(chipAnims, glowScale)
                        1 -> Page2Illustration(chipAnims, glowScale)
                        else -> Page3Illustration(chipAnims, glowScale)
                    }
                }

                // ── Bottom sheet ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { translationY = bottomY; alpha = bottomAlpha }
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                        .background(BottomSheetBg)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = pageTitle(pageIndex),
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = pageSubtitle(pageIndex),
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // Dot indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { index ->
                            val isActive = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .height(5.dp)
                                    .width(if (isActive) 22.dp else 5.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(
                                        if (isActive) Purple
                                        else Color.White.copy(alpha = 0.25f)
                                    )
                                    .animateContentSize()
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                val nativeAd = OnboardingAdManager.consumeNativeAd()
                                if (nativeAd != null) {
                                    currentNativeAd = nativeAd
                                    pendingAction = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                    showNativeAdOverlay = true
                                } else {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                    OnboardingAdManager.preloadNextNativeAd(context)
                                }
                            } else {
                                OnboardingAdManager.showInterstitialAd(activity) {
                                    scope.launch {
                                        BrowserPreferences(context.applicationContext).setFirstLaunchComplete()
                                    }
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 2) stringResource(R.string.onboarding_get_started) else stringResource(R.string.onboarding_next),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(28.dp))
                }
            }
        }

        if (showNativeAdOverlay) {
            NativeAdOverlay(
                nativeAd = currentNativeAd,
                onClose = {
                    showNativeAdOverlay = false
                    currentNativeAd = null
                    pendingAction?.invoke()
                    pendingAction = null
                    OnboardingAdManager.preloadNextNativeAd(context)
                }
            )
        }
    }
}

// ─── Titles ───────────────────────────────────────────────────────────────────

@Composable
private fun pageTitle(pageIndex: Int): String = when (pageIndex) {
    0 -> stringResource(R.string.onboarding_page1_title)
    1 -> stringResource(R.string.onboarding_page2_title)
    else -> stringResource(R.string.onboarding_page3_title)
}

@Composable
private fun pageSubtitle(pageIndex: Int): String = when (pageIndex) {
    0 -> stringResource(R.string.onboarding_page1_subtitle)
    1 -> stringResource(R.string.onboarding_page2_subtitle)
    else -> stringResource(R.string.onboarding_page3_subtitle)
}

// ─── Glow helper ─────────────────────────────────────────────────────────────

@Composable
private fun PurpleGlow(size: Int, scale: Float) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x556C47D9), Color(0x006C47D9)),
                    center = center,
                    radius = this.size.minDimension / 2f
                )
            )
        }
    }
}

// ─── PAGE 1: Browse Safely Everywhere ────────────────────────────────────────

private data class ChipData(
    val iconRes: Int,
    val iconColor: Color,
    val iconBg: Color,
    val title: String,
    val subtitle: String
)

@Composable
private fun Page1Illustration(chipAnims: List<Pair<Float, Float>>, glowScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            PurpleGlow(size = 160, scale = glowScale)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Purple.copy(alpha = 0.18f))
                    .border(1.dp, Purple.copy(alpha = 0.45f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.onboard_shield),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Purple)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        val chips = listOf(
            ChipData(R.drawable.onboard_eye_off, Color(0xFFff7aaa), Color(0x26FF5A82), stringResource(R.string.onboarding_chip1_title), stringResource(R.string.onboarding_chip1_subtitle)),
            ChipData(R.drawable.onboard_lock, Color(0xFF6ab0ff), Color(0x263C8CFF), stringResource(R.string.onboarding_chip2_title), stringResource(R.string.onboarding_chip2_subtitle)),
            ChipData(R.drawable.onboard_incognito, Color(0xFFb39dff), Color(0x336C47D9), stringResource(R.string.onboarding_chip3_title), stringResource(R.string.onboarding_chip3_subtitle)),
        )

        chips.forEachIndexed { i, chip ->
            val (ty, alpha) = chipAnims[i]
            Box(modifier = Modifier.graphicsLayer { translationY = ty; this.alpha = alpha }) {
                FeatureChip(chip)
            }
            if (i < chips.lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FeatureChip(chip: ChipData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(chip.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(chip.iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(chip.iconColor)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = chip.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = chip.subtitle, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── PAGE 2: Fast Browsing, Zero Tracking ────────────────────────────────────

@Composable
private fun Page2Illustration(chipAnims: List<Pair<Float, Float>>, glowScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            PurpleGlow(size = 140, scale = glowScale)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Purple),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.onboard_rocket),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        val statData = listOf(
            Triple("2×", Color(0xFFa78bfa), 0.80f),
            Triple("60%", Color(0xFF6ab0ff), 0.60f),
            Triple("0", Color(0xFF4ade80), 1.00f),
        )
        val statLabels = listOf(stringResource(R.string.onboarding_stat1_label), stringResource(R.string.onboarding_stat2_label), stringResource(R.string.onboarding_stat3_label))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statData.forEachIndexed { i, (value, color, progress) ->
                val (ty, alpha) = chipAnims[i]
                StatCard(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { translationY = ty; this.alpha = alpha },
                    value = value,
                    valueColor = color,
                    label = statLabels[i],
                    progress = progress,
                    barColor = color
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Speed bar card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.onboard_bolt),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Purple)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.onboarding_speed_label),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            // Gradient bar with dot at end
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(Purple, Color(0xFF9B72FF)))),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(text = "88%", color = PurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    valueColor: Color,
    label: String,
    progress: Float,
    barColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.12f)
        )
    }
}

// ─── PAGE 3: Your Browser, Your Rules ────────────────────────────────────────

@Composable
private fun Page3Illustration(chipAnims: List<Pair<Float, Float>>, glowScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            PurpleGlow(size = 140, scale = glowScale)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Purple.copy(alpha = 0.2f))
                    .border(1.dp, Purple.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.onboard_browser),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(PurpleLight)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        val (ty0, a0) = chipAnims[0]
        val (ty1, a1) = chipAnims[1]
        val (ty2, a2) = chipAnims[2]

        val rows = listOf(
            FeatureRowData("1", Purple, stringResource(R.string.onboarding_feature1_title), stringResource(R.string.onboarding_feature1_subtitle), stringResource(R.string.onboarding_feature1_badge), Purple),
            FeatureRowData("2", Color(0xFF1d4ed8), stringResource(R.string.onboarding_feature2_title), stringResource(R.string.onboarding_feature2_subtitle), stringResource(R.string.onboarding_feature2_badge), Color(0xFF1d4ed8)),
            FeatureRowData("3", Color(0xFF15803d), stringResource(R.string.onboarding_feature3_title), stringResource(R.string.onboarding_feature3_subtitle), stringResource(R.string.onboarding_feature3_badge), Color(0xFF15803d)),
        )
        val rowAnims = listOf(ty0 to a0, ty1 to a1, ty2 to a2)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
        ) {
            rows.forEachIndexed { i, row ->
                val (ty, alpha) = rowAnims[i]
                FeatureRow(
                    data = row,
                    modifier = Modifier.graphicsLayer { translationY = ty; this.alpha = alpha }
                )
                if (i < rows.lastIndex) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.07f), thickness = 1.dp)
                }
            }
        }
    }
}

private data class FeatureRowData(
    val number: String,
    val numberBg: Color,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val badgeColor: Color
)

@Composable
private fun FeatureRow(data: FeatureRowData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(data.numberBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = data.number, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = data.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = data.subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(data.badgeColor.copy(alpha = 0.25f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(text = data.badgeText, color = data.badgeColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
