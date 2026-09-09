package com.swiftbrowser.fast.secure.presentation.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.ads.AdManager
import com.swiftbrowser.fast.secure.core.ads.BannerAdView
import com.swiftbrowser.fast.secure.core.base.UiState
import com.swiftbrowser.fast.secure.core.utils.toFormattedDate
import com.swiftbrowser.fast.secure.core.utils.toFormattedTime
import com.swiftbrowser.fast.secure.domain.model.HistoryItem
import com.swiftbrowser.fast.secure.presentation.navigation.Screen
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBorder
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBorderStrong
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme
import com.swiftbrowser.fast.secure.presentation.theme.SwiftNavy
import com.swiftbrowser.fast.secure.presentation.theme.SwiftNavyCard
import com.swiftbrowser.fast.secure.presentation.theme.SwiftNavyDark
import com.swiftbrowser.fast.secure.presentation.theme.SwiftRed
import com.swiftbrowser.fast.secure.presentation.theme.SwiftSurface1
import java.util.Calendar

private sealed class HistoryEntry {
    data class Header(val label: String) : HistoryEntry()
    data class Item(val item: HistoryItem) : HistoryEntry()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onHistoryItemClicked: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<HistoryItem?>(null) }

    val todayLabel = stringResource(R.string.history_today)
    val yesterdayLabel = stringResource(R.string.history_yesterday)
    val thisWeekLabel = stringResource(R.string.history_this_week)

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SwiftNavyCard,
            title = {
                Text(
                    text = stringResource(R.string.history_clear_action),
                    color = Color.White.copy(alpha = 0.9f),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.history_clear_confirm),
                    color = Color.White.copy(alpha = 0.6f),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onClearHistory()
                    showClearDialog = false
                }) {
                    Text(stringResource(R.string.action_delete), color = SwiftRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = Color.White.copy(alpha = 0.5f))
                }
            },
        )
    }

    selectedItem?.let { item ->
        DeleteHistoryItemSheet(
            onDismiss = { selectedItem = null },
            onDelete = {
                viewModel.onDeleteHistoryItem(item.id)
                selectedItem = null
            },
        )
    }

    Scaffold(
        containerColor = SwiftNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_history),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W600,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showClearDialog = true }) {
                        Text(
                            text = stringResource(R.string.history_clear_action),
                            color = SwiftRed,
                            fontSize = 13.sp,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftNavyDark,
                    scrolledContainerColor = SwiftNavyDark,
                ),
            )
        },
        bottomBar = {
            Column {
                BannerAdView(adId = AdManager.getHistoryBannerId())
                HistoryNavBar(onNavigate = onNavigate)
            }
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Success -> {
                val entries = remember(state.data, todayLabel, yesterdayLabel, thisWeekLabel) {
                    buildHistoryEntries(state.data, todayLabel, yesterdayLabel, thisWeekLabel)
                }

                LazyColumn(contentPadding = paddingValues) {
                    items(entries) { entry ->
                        when (entry) {
                            is HistoryEntry.Header -> {
                                Text(
                                    text = entry.label.uppercase(),
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W500,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }

                            is HistoryEntry.Item -> {
                                HistoryRow(
                                    item = entry.item,
                                    onClick = { onHistoryItemClicked(entry.item.url) },
                                    onLongClick = { selectedItem = entry.item },
                                )
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.05f),
                                    thickness = 0.5.dp,
                                )
                            }
                        }
                    }
                }
            }

            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp),
                        )
                        Text(
                            text = stringResource(R.string.history_empty),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                        )
                        Text(
                            text = stringResource(R.string.history_empty_hint),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun HistoryRow(
    item: HistoryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Globe icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SwiftSurface1),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.url,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = item.visitedAt.toFormattedTime(),
            color = Color.White.copy(alpha = 0.25f),
            fontSize = 11.sp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteHistoryItemSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SwiftNavyCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SwiftBorderStrong),
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDelete)
                .padding(16.dp)
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.history_delete_item),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun HistoryNavBar(onNavigate: (String) -> Unit) {
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
                onClick = { onNavigate(Screen.Home.route) }
            )
            NavTab(
                activeIconRes = R.drawable.ic_nav_search_active,
                inactiveIconRes = R.drawable.ic_nav_search,
                label = stringResource(R.string.url_bar_search),
                isActive = false,
                onClick = { onNavigate(Screen.Browser.createRoute("")) }
            )
            NavTab(
                activeIconRes = R.drawable.ic_nav_downloads_active,
                inactiveIconRes = R.drawable.ic_nav_downloads,
                label = stringResource(R.string.nav_downloads),
                isActive = false,
                onClick = { onNavigate(Screen.Downloads.route) }
            )
            NavTab(
                activeIconRes = R.drawable.ic_nav_settings_active,
                inactiveIconRes = R.drawable.ic_nav_settings,
                label = stringResource(R.string.nav_settings),
                isActive = false,
                onClick = { onNavigate(Screen.Settings.route) }
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
    val dotColor = Color(0xFF6C47D9)
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        color = dotColor,
                        radius = 3.dp.toPx(),
                        center = Offset(size.width / 2f, 3.dp.toPx())
                    )
                }
            }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(if (isActive) activeIconRes else inactiveIconRes),
            contentDescription = label,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize = 8.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun buildHistoryEntries(
    items: List<HistoryItem>,
    todayLabel: String,
    yesterdayLabel: String,
    thisWeekLabel: String,
): List<HistoryEntry> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = cal.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L
    val weekStart = todayStart - 7 * 86_400_000L

    val result = mutableListOf<HistoryEntry>()
    var currentHeader = ""

    for (item in items) {
        val header = when {
            item.visitedAt >= todayStart -> todayLabel
            item.visitedAt >= yesterdayStart -> yesterdayLabel
            item.visitedAt >= weekStart -> thisWeekLabel
            else -> item.visitedAt.toFormattedDate()
        }
        if (header != currentHeader) {
            result.add(HistoryEntry.Header(header))
            currentHeader = header
        }
        result.add(HistoryEntry.Item(item))
    }

    return result
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E1A)
@Composable
private fun HistoryScreenPreview() {
    SwiftBrowserTheme {
        HistoryScreen(onHistoryItemClicked = {}, onNavigate = {}, onNavigateUp = {})
    }
}
