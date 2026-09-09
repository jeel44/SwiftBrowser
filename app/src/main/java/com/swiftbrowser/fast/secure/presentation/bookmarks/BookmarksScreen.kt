package com.swiftbrowser.fast.secure.presentation.bookmarks

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
import com.swiftbrowser.fast.secure.core.utils.toDisplayUrl
import com.swiftbrowser.fast.secure.domain.model.Bookmark
import com.swiftbrowser.fast.secure.presentation.components.NativeAdCard
import com.swiftbrowser.fast.secure.presentation.navigation.Screen
import com.swiftbrowser.fast.secure.presentation.theme.SwiftAmber
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBlue
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBlueDark
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBorder
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBorderStrong
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme
import com.swiftbrowser.fast.secure.presentation.theme.SwiftCyan
import com.swiftbrowser.fast.secure.presentation.theme.SwiftGreen
import com.swiftbrowser.fast.secure.presentation.theme.SwiftNavy
import com.swiftbrowser.fast.secure.presentation.theme.SwiftNavyCard
import com.swiftbrowser.fast.secure.presentation.theme.SwiftPurple
import com.swiftbrowser.fast.secure.presentation.theme.SwiftRed
import com.swiftbrowser.fast.secure.presentation.theme.SwiftSurface1
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBookmarkClicked: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    var selectedBookmark by remember { mutableStateOf<Bookmark?>(null) }

    selectedBookmark?.let { bookmark ->
        BookmarkOptionsSheet(
            bookmark = bookmark,
            onDismiss = { selectedBookmark = null },
            onOpen = { onBookmarkClicked(bookmark.url); selectedBookmark = null },
            onShare = {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, bookmark.url)
                        },
                        null,
                    )
                )
                selectedBookmark = null
            },
            onEdit = { selectedBookmark = null },
            onDelete = { viewModel.onDeleteBookmark(bookmark); selectedBookmark = null },
        )
    }

    Scaffold(
        containerColor = SwiftNavy,
        bottomBar = {
            Column {
                BannerAdView(adId = AdManager.getBookmarksBannerId())
                BookmarksNavBar(onNavigate = onNavigate)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // ── Header ──
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
                Text(
                    text = stringResource(R.string.nav_bookmarks),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SwiftSurface1)
                        .border(0.5.dp, SwiftBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.bookmarks_search_hint),
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp,
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(SwiftBlue),
                        )
                    }
                }
            }

            // ── Content ──
            when (val state = uiState) {
                is UiState.Success -> {
                    val bookmarks = state.data
                    LazyColumn {
                        itemsIndexed(bookmarks, key = { _, b -> b.id }) { index, bookmark ->
                            BookmarkRow(
                                bookmark = bookmark,
                                onClick = { onBookmarkClicked(bookmark.url) },
                                onLongClick = { selectedBookmark = bookmark },
                            )
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.05f),
                                thickness = 0.5.dp,
                            )
                            if ((index + 1) % 5 == 0) {
                                NativeAdCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                            }
                        }
                    }
                }

                is UiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(56.dp),
                            )
                            Text(
                                text = stringResource(R.string.bookmarks_empty),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 16.sp,
                            )
                            Text(
                                text = stringResource(R.string.bookmarks_empty_hint),
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
}

@Composable
private fun BookmarkRow(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val domain = bookmark.url.toDisplayUrl()
    val letter = domain.firstOrNull()?.uppercaseChar()?.toString() ?: "B"
    val letterColor = bookmarkDomainColor(domain)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Favicon box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(letterColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter,
                color = letterColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
            )
        }

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bookmark.title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = bookmark.url,
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkOptionsSheet(
    bookmark: Bookmark,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
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
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            BookmarkOptionRow(icon = Icons.Filled.Bookmark, label = stringResource(R.string.action_open), onClick = onOpen)
            HorizontalDivider(color = SwiftBorder, thickness = 0.5.dp)
            BookmarkOptionRow(icon = Icons.Filled.Share, label = stringResource(R.string.action_share), onClick = onShare)
            HorizontalDivider(color = SwiftBorder, thickness = 0.5.dp)
            BookmarkOptionRow(icon = Icons.Filled.Edit, label = stringResource(R.string.action_edit), onClick = onEdit)
            HorizontalDivider(color = SwiftBorder, thickness = 0.5.dp)
            BookmarkOptionRow(icon = Icons.Filled.Delete, label = stringResource(R.string.action_delete), onClick = onDelete)
        }
    }
}

@Composable
private fun BookmarkOptionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp),
        )
        Text(text = label, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
    }
}

@Composable
private fun BookmarksNavBar(onNavigate: (String) -> Unit) {
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
                onClick = { onNavigate(Screen.Browser.createRoute("https://www.google.com")) }
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

private fun bookmarkDomainColor(domain: String): Color {
    val palette = listOf(SwiftBlue, SwiftBlueDark, SwiftCyan, SwiftGreen, SwiftRed, SwiftAmber, SwiftPurple)
    return palette[abs(domain.hashCode()) % palette.size]
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E1A)
@Composable
private fun BookmarksScreenPreview() {
    SwiftBrowserTheme {
        BookmarksScreen(onBookmarkClicked = {}, onNavigate = {}, onNavigateUp = {})
    }
}
