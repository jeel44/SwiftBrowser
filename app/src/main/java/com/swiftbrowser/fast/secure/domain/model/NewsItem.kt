package com.swiftbrowser.fast.secure.domain.model

data class NewsItem(
    val id: Int,
    val title: String,
    val source: String,
    val timeAgo: String,
    val thumbnailEmoji: String,
    val url: String,
    val category: NewsCategory = NewsCategory.TOP,
    val imageUrl: String = "",
    val isAd: Boolean = false,
    val adTitle: String = "",
    val adDescription: String = "",
    val adCta: String = "Learn More",
)
