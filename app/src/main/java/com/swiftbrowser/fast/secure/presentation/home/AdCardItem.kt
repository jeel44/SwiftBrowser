package com.swiftbrowser.fast.secure.presentation.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swiftbrowser.fast.secure.R
import com.swiftbrowser.fast.secure.domain.model.NewsItem
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme

private val LightAdBackground = Color(0xFFFFFDE7)  // amber-50
private val DarkAdBackground = Color(0xFF3D3000)    // dark amber
private val AdBadgeColor = Color(0xFFE65100)        // deep orange

/**
 * Placeholder native-ad card rendered as part of the news feed.
 *
 * Uses an amber/yellow background so it is visually distinct from editorial content.
 * The "Ad" badge follows Play Store policy requirements (clearly labeled).
 *
 * TODO(Phase 5): Replace with a real AdMob native ad view via AndroidView.
 */
@Composable
fun AdCardItem(
    item: NewsItem,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSystemInDarkTheme()) DarkAdBackground else LightAdBackground

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // "Ad" badge + title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdBadge()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.adTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            if (item.adDescription.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.adDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { /* TODO(Phase 5): track CTA click and open ad URL */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdBadgeColor,
                    contentColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = item.adCta.ifBlank { stringResource(R.string.ad_learn_more) },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Small pill badge that clearly labels content as an advertisement. */
@Composable
private fun AdBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = AdBadgeColor,
    ) {
        Text(
            text = stringResource(R.string.ad_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ──────────────────────────────── Previews ────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AdCardItemPreview() {
    SwiftBrowserTheme {
        AdCardItem(
            item = NewsItem(
                id = -1,
                title = "",
                source = "",
                timeAgo = "",
                thumbnailEmoji = "",
                url = "",
                isAd = true,
                adTitle = "Flash Sale: Up to 40% Off on Smartphones",
                adDescription = "Samsung, OnePlus, Xiaomi & more — Limited time offer on Flipkart",
                adCta = "Shop Now",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
