package com.swiftbrowser.fast.secure.data.remote.rss

import com.swiftbrowser.fast.secure.domain.model.NewsCategory

data class RssSource(
    val name: String,
    val url: String,
    val category: NewsCategory,
)

object NewsSources {
    val DEFAULT = listOf(
        RssSource("Times of India",  "https://timesofindia.indiatimes.com/rssfeedstopstories.cms",         NewsCategory.TOP),
        RssSource("BBC World",       "http://feeds.bbci.co.uk/news/world/rss.xml",                         NewsCategory.WORLD),
        RssSource("ESPNcricinfo",    "https://www.espncricinfo.com/rss/content/story/feeds/0.xml",          NewsCategory.SPORTS),
        RssSource("Economic Times",  "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms", NewsCategory.FINANCE),
        RssSource("TechCrunch",      "https://techcrunch.com/feed/",                                       NewsCategory.TECH),
    )
}
