package com.swiftbrowser.fast.secure.presentation.downloads

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.AdManager
import com.swiftbrowser.fast.secure.core.ads.BannerAdView
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.data.local.entity.DownloadStatus
import com.swiftbrowser.fast.secure.domain.model.DownloadItem

private val BgColor = Color(0xFF0A0010)
private val AccentPurple = Color(0xFF6C47D9)
private val tabs = listOf(R.string.downloads_tab_progress, R.string.downloads_tab_player)

@Composable
fun DownloadsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgColor,
        bottomBar = {
            Column {
                BannerAdView(adId = AdManager.getDownloadsBannerId())
                DownloadsNavBar(
                    onHomeClick = onNavigateToHome,
                    onSearchClick = onNavigateToSearch,
                    onSettingsClick = onNavigateToSettings,
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgColor,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(AccentPurple)
                    )
                },
                divider = {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 0.5.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, titleRes ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                    ) {
                        Text(
                            text = stringResource(titleRes),
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> ProgressTabContent(uiState = uiState)
                1 -> PlanetEmptyState(message = stringResource(R.string.downloads_saved_empty))
            }
        }
    }
}

@Composable
private fun ProgressTabContent(uiState: UiState<List<DownloadItem>>) {
    when (val state = uiState) {
        is UiState.Success -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id }) { item ->
                    DownloadListItem(item = item)
                }
            }
        }
        else -> PlanetEmptyState(message = stringResource(R.string.downloads_no_active))
    }
}

@Composable
private fun PlanetEmptyState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2640))
                    .border(2.dp, Color(0xFF4A4080), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(rotationZ = -20f)
                    .drawBehind {
                        drawOval(
                            color = AccentPurple,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
            )
            Text(
                text = "✦",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 8.dp, y = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DownloadListItem(item: DownloadItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1C1C1E)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = item.fileName,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildSizeStatus(item),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        when (item.status) {
            DownloadStatus.RUNNING, DownloadStatus.PENDING, DownloadStatus.PAUSED -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AccentPurple,
                    strokeWidth = 2.dp
                )
            }
            DownloadStatus.SUCCESSFUL -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF34A853),
                    modifier = Modifier.size(20.dp)
                )
            }
            DownloadStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun buildSizeStatus(item: DownloadItem): String {
    val sizeStr = if (item.fileSize > 0) formatFileSize(item.fileSize) else ""
    val statusStr = item.status.name.lowercase().replaceFirstChar { it.uppercase() }
    return if (sizeStr.isNotEmpty()) "$sizeStr · $statusStr" else statusStr
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
    else -> "$bytes B"
}

// ─────────────────────────────── Bottom Nav ───────────────────────────────

@Composable
private fun DownloadsNavBar(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
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
            NavTab(
                activeIconRes = R.drawable.ic_nav_home_active,
                inactiveIconRes = R.drawable.ic_nav_home,
                label = stringResource(R.string.nav_home),
                isActive = false,
                onClick = onHomeClick
            )
            NavTab(
                activeIconRes = R.drawable.ic_nav_search_active,
                inactiveIconRes = R.drawable.ic_nav_search,
                label = stringResource(R.string.url_bar_search),
                isActive = false,
                onClick = onSearchClick
            )
            NavTab(
                activeIconRes = R.drawable.ic_nav_downloads_active,
                inactiveIconRes = R.drawable.ic_nav_downloads,
                label = stringResource(R.string.nav_downloads),
                isActive = true,
                onClick = {}
            )
            NavTab(
                activeIconRes = R.drawable.ic_nav_settings_active,
                inactiveIconRes = R.drawable.ic_nav_settings,
                label = stringResource(R.string.nav_settings),
                isActive = false,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun RowScope.NavTab(
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
