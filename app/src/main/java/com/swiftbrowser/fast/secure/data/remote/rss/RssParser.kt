package com.swiftbrowser.fast.secure.data.remote.rss

import android.util.Xml
import com.swiftbrowser.fast.secure.domain.model.NewsCategory
import com.swiftbrowser.fast.secure.domain.model.NewsItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssParser @Inject constructor() {

    fun parse(xml: String, sourceName: String, category: NewsCategory): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(StringReader(xml))
            }
            var inItem = false
            var title = ""
            var link = ""
            var pubDate = ""
            var imageUrl = ""
            var idCounter = 0

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT && items.size < 15) {
                val tag = parser.name ?: ""
                when (eventType) {
                    XmlPullParser.START_TAG -> when (tag) {
                        "item" -> { inItem = true; title = ""; link = ""; pubDate = ""; imageUrl = "" }
                        "title" -> if (inItem) title = safeNextText(parser)
                        "link" -> if (inItem && link.isEmpty()) link = safeNextText(parser)
                        "pubDate" -> if (inItem) pubDate = safeNextText(parser)
                        "media:content" -> if (inItem) imageUrl = parser.getAttributeValue(null, "url") ?: imageUrl
                        "enclosure" -> if (inItem && imageUrl.isEmpty()) imageUrl = parser.getAttributeValue(null, "url") ?: ""
                    }
                    XmlPullParser.END_TAG -> if (tag == "item" && inItem) {
                        if (title.isNotEmpty() && link.isNotEmpty()) {
                            items += NewsItem(
                                id = idCounter++,
                                title = title,
                                source = sourceName,
                                timeAgo = formatPubDate(pubDate),
                                thumbnailEmoji = "",
                                url = link,
                                category = category,
                                imageUrl = imageUrl,
                            )
                        }
                        inItem = false
                    }
                }
                eventType = parser.next()
            }
        } catch (_: XmlPullParserException) {
        } catch (_: Exception) {
        }
        return items
    }

    private fun safeNextText(parser: XmlPullParser): String =
        try { parser.nextText().trim() } catch (_: Exception) { "" }

    private fun formatPubDate(pubDate: String): String {
        if (pubDate.isEmpty()) return ""
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            val date = sdf.parse(pubDate) ?: return ""
            val diff = System.currentTimeMillis() - date.time
            when {
                diff < 3_600_000L  -> "${diff / 60_000}m ago"
                diff < 86_400_000L -> "${diff / 3_600_000}h ago"
                else               -> "${diff / 86_400_000}d ago"
            }
        } catch (_: Exception) { "" }
    }
}
