package com.swiftbrowser.fast.secure.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.domain.model.SpeedDialSite
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme

/**
 * Bottom sheet shown when a speed-dial tile is long-pressed.
 * Offers Edit and Remove actions for the selected [site].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedDialBottomSheet(
    site: SpeedDialSite,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Sheet title
            Text(
                text = site.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                text = site.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            SpeedDialSheetAction(
                icon = Icons.Default.Edit,
                label = stringResource(R.string.speed_dial_edit),
                onClick = { onEdit(); onDismiss() },
            )
            SpeedDialSheetAction(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.speed_dial_remove),
                tint = MaterialTheme.colorScheme.error,
                onClick = { onRemove(); onDismiss() },
            )

            // Extra space for gesture bar
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SpeedDialSheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
            )
        }
    }
}

// ──────────────────────────────── Previews ────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SpeedDialBottomSheetPreview() {
    SwiftBrowserTheme {
        SpeedDialBottomSheet(
            site = SpeedDialSite(1, "Google", "https://google.com", 0xFFEA4335L, "G"),
            onDismiss = {},
            onEdit = {},
            onRemove = {},
        )
    }
}
