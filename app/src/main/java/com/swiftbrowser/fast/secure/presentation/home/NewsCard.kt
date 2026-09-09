package com.swiftbrowser.fast.secure.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftbrowser.fast.secure.domain.model.NewsCategory
import com.swiftbrowser.fast.secure.domain.model.NewsItem
import com.swiftbrowser.fast.secure.presentation.theme.SwiftAmber
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBlue
import com.swiftbrowser.fast.secure.presentation.theme.SwiftBrowserTheme
import com.swiftbrowser.fast.secure.presentation.theme.SwiftCyan
import com.swiftbrowser.fast.secure.presentation.theme.SwiftGreen

@Composable
fun NewsCard(
    item: NewsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NewsThumbnail(category = item.category)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${item.source}  ·  ${item.timeAgo}",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                )
            }
        }
        HorizontalDivider(
            color = Color.White.copy(alpha = 0.06f),
            thickness = 0.5.dp,
        )
    }
}

@Composable
private fun NewsThumbnail(
    category: NewsCategory,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (category) {
        NewsCategory.TOP    -> SwiftBlue.copy(alpha = 0.15f)
        NewsCategory.WORLD  -> SwiftBlue.copy(alpha = 0.15f)
        NewsCategory.SPORTS -> SwiftGreen.copy(alpha = 0.15f)
        NewsCategory.FINANCE -> SwiftAmber.copy(alpha = 0.15f)
        NewsCategory.TECH   -> SwiftCyan.copy(alpha = 0.15f)
    }
    Box(
        modifier = modifier
            .size(width = 60.dp, height = 52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun NewsCardPreview() {
    SwiftBrowserTheme {
        NewsCard(
            item = NewsItem(
                id = 1,
                title = "India seal ODI series against England with stunning 7-wicket win in Mumbai",
                source = "ESPNcricinfo",
                timeAgo = "4h ago",
                thumbnailEmoji = "🏏",
                url = "https://espncricinfo.com",
                category = NewsCategory.SPORTS,
            ),
            onClick = {},
        )
    }
}
