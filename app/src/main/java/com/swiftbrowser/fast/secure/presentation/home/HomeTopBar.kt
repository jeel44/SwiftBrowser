package com.swiftbrowser.fast.secure.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme

/**
 * Top app bar for the Home screen.
 *
 * Left:  SwiftBrowser "S" logo + app name
 * Right: tab-counter pill → three-dot overflow menu
 *
 * The overflow menu matches the spec: New Tab, Incognito Tab, divider, History, Downloads,
 * Settings, divider, Share, Find in Page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    tabCount: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    onTabsClick: () -> Unit,
    onNewTab: () -> Unit,
    onIncognitoTab: () -> Unit,
    onHistoryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFindInPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        navigationIcon = {
            SwiftLogoMark(modifier = Modifier.padding(start = 12.dp))
        },
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            // Tab counter pill
            TabCounterButton(
                count = tabCount,
                onClick = onTabsClick,
            )

            // Three-dot overflow
            Box {
                IconButton(onClick = { overflowExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                HomeOverflowMenu(
                    expanded = overflowExpanded,
                    onDismiss = { overflowExpanded = false },
                    onNewTab = { overflowExpanded = false; onNewTab() },
                    onIncognitoTab = { overflowExpanded = false; onIncognitoTab() },
                    onHistory = { overflowExpanded = false; onHistoryClick() },
                    onDownloads = { overflowExpanded = false; onDownloadsClick() },
                    onSettings = { overflowExpanded = false; onSettingsClick() },
                    onFindInPage = { overflowExpanded = false; onFindInPage() },
                )
            }
        },
    )
}

// ──────────────────────────────── Swift "S" logo mark ────────────────────────────────

/**
 * Circular brand logo — a white "S" on the SwiftBlue primary colour. Renders
 * without any external asset so it's always crisp at every density.
 */
@Composable
fun SwiftLogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "S",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

// ──────────────────────────────── Tab counter ────────────────────────────────

/**
 * Rounded-rectangle button that shows the current open tab count.
 * Caps display at "99" to keep layout stable at very high counts.
 */
@Composable
private fun TabCounterButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayCount = count.coerceIn(1, 99).toString()
    Surface(
        onClick = onClick,
        modifier = modifier.padding(end = 4.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 7.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayCount,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ──────────────────────────────── Overflow menu ────────────────────────────────

@Composable
private fun HomeOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onIncognitoTab: () -> Unit,
    onHistory: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit,
    onFindInPage: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.tabs_new_tab)) },
            leadingIcon = { Icon(Icons.Default.Tab, contentDescription = null) },
            onClick = onNewTab,
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.tab_new_incognito)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Tab,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            },
            onClick = onIncognitoTab,
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.nav_history)) },
            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
            onClick = onHistory,
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.nav_downloads)) },
            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
            onClick = onDownloads,
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.nav_settings)) },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = onSettings,
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_find_in_page)) },
            leadingIcon = { Icon(Icons.Default.FindInPage, contentDescription = null) },
            onClick = onFindInPage,
        )
    }
}

// ──────────────────────────────── Previews ────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    SwiftBrowserTheme {
        HomeTopBar(
            tabCount = 3,
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            onTabsClick = {},
            onNewTab = {},
            onIncognitoTab = {},
            onHistoryClick = {},
            onDownloadsClick = {},
            onSettingsClick = {},
            onFindInPage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwiftLogoMarkPreview() {
    SwiftBrowserTheme { SwiftLogoMark() }
}
