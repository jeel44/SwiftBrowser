package com.swiftbrowser.fast.secure.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.core.utils.Constants
import com.swiftbrowser.fast.secure.domain.model.SpeedDialSite
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme

/**
 * "Quick Access" section: horizontal scrollable row of speed-dial tiles.
 *
 * Long-pressing a tile shows a [SpeedDialBottomSheet] with Edit / Remove actions.
 * The "+" add-site button always appears at the trailing end.
 */
@Composable
fun SpeedDialSection(
    sites: List<SpeedDialSite>,
    onSiteClick: (SpeedDialSite) -> Unit,
    onAddClick: () -> Unit,
    onSiteEdit: (SpeedDialSite) -> Unit,
    onSiteRemove: (SpeedDialSite) -> Unit,
    modifier: Modifier = Modifier,
) {
    var longPressedSite by remember { mutableStateOf<SpeedDialSite?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Text(
            text = stringResource(R.string.home_quick_access),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        // Horizontally scrollable speed-dial row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(items = sites, key = { it.id }) { site ->
                SpeedDialItem(
                    site = site,
                    onClick = { onSiteClick(site) },
                    onLongClick = { longPressedSite = site },
                )
            }
            item(key = "add_button") {
                SpeedDialAddButton(onClick = onAddClick)
            }
        }
    }

    // Long-press bottom sheet
    longPressedSite?.let { site ->
        SpeedDialBottomSheet(
            site = site,
            onDismiss = { longPressedSite = null },
            onEdit = { onSiteEdit(site) },
            onRemove = { onSiteRemove(site) },
        )
    }
}

// ──────────────────────────────── Previews ────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SpeedDialSectionPreview() {
    SwiftBrowserTheme {
        SpeedDialSection(
            sites = Constants.DEFAULT_SPEED_DIAL_SITES,
            onSiteClick = {},
            onAddClick = {},
            onSiteEdit = {},
            onSiteRemove = {},
        )
    }
}
