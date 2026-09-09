package com.swiftbrowser.fast.secure.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftbrowser.fast.secure.domain.model.SpeedDialSite
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme

/**
 * Single speed-dial tile: a 52dp × 52dp colored tile with [SpeedDialSite.iconLabel]
 * centred inside, and [SpeedDialSite.name] below.
 *
 * Long-press triggers [onLongClick] which the parent uses to show the edit/remove bottom sheet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedDialItem(
    site: SpeedDialSite,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(site.iconColor)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = site.iconLabel,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = site.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * The "+" add-site button at the trailing end of the speed-dial row.
 */
@Composable
fun SpeedDialAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
            )
        }
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ──────────────────────────────── Previews ────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SpeedDialItemPreview() {
    SwiftBrowserTheme {
        SpeedDialItem(
            site = SpeedDialSite(1, "Google", "https://google.com", 0xFFEA4335L, "G"),
            onClick = {},
            onLongClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeedDialAddButtonPreview() {
    SwiftBrowserTheme { SpeedDialAddButton(onClick = {}) }
}
