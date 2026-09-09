package com.swiftbrowser.fast.secure.presentation.language

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.LanguageAdManager
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.core.language.LocaleHelper
import com.swiftbrowser.fast.secure.data.datastore.BrowserPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class Language(val name: String, val flagRes: Int)

@Composable
fun LanguageScreen(
    browserPreferences: BrowserPreferences,
    onNavigateToOnboarding: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val viewModel: LanguageViewModel = hiltViewModel()
    val currentLanguageCode by viewModel.browserPreferences.selectedLanguage.collectAsState(initial = "system")

    LaunchedEffect(Unit) {
        LanguageAdManager.preload(context)
    }

    val languages = listOf(
        Language("System language", R.drawable.flag_system),
        Language("English",         R.drawable.flag_en),
        Language("हिन्दी",           R.drawable.flag_hi),
        Language("Bahasa Indonesia", R.drawable.flag_id),
        Language("Deutsch",          R.drawable.flag_de),
        Language("Português",        R.drawable.flag_pt),
        Language("Русский",          R.drawable.flag_ru),
        Language("繁體中文",           R.drawable.flag_zh)
    )

    fun navigateToOnboarding() {
        LanguageAdManager.showOnLanguageSelect(activity) {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0010))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0010))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.language_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { navigateToOnboarding() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47D9)),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.language_next),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Language list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(languages) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1C1C1E))
                            .clickable {
                                val code = LocaleHelper.getLanguageCode(language.name)
                                scope.launch {
                                    browserPreferences.setSelectedLanguage(code)
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        activity.recreate()
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = language.flagRes),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(width = 32.dp, height = 24.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = language.name,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (LocaleHelper.getLanguageCode(language.name) == currentLanguageCode) Color(0xFF6C47D9)
                                    else Color(0xFF2A2A2E)
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (LocaleHelper.getLanguageCode(language.name) == currentLanguageCode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF6C47D9),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(12.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                AdView(ctx).apply {
                                    setAdSize(AdSize.BANNER)
                                    adUnitId = Constants.AdUnitIds.BANNER
                                    loadAd(AdRequest.Builder().build())
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // OPEN button
        Button(
            onClick = { navigateToOnboarding() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(52.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47D9))
        ) {
            Text(
                text = stringResource(R.string.language_open),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
