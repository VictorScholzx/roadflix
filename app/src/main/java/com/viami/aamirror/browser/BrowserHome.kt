package com.viami.aamirror.browser

import android.content.Context
import android.content.res.Configuration
import androidx.car.app.CarContext
import com.viami.aamirror.core.AppTheme
import com.viami.aamirror.core.History
import com.viami.aamirror.core.HomePage
import com.viami.aamirror.core.HomeTile
import com.viami.aamirror.core.MirrorSettings
import com.viami.aamirror.core.Palette
import com.viami.aamirror.core.RecentLink
import com.viami.aamirror.core.SentLink
import com.viami.aamirror.core.UrlResolver
import com.viami.aamirror.setup.BookmarkStore
import com.viami.aamirror.setup.HistoryStore

/** Builds the start page from the favourites, their cached icons, the history and the chosen theme. */
object BrowserHome {

    /** How many recent pages the start page lists. */
    private const val RECENT_ROWS = 5

    fun html(context: Context): String {
        val now = System.currentTimeMillis()
        val tiles = BookmarkStore.load(context).map { bookmark ->
            HomeTile(
                title = bookmark.title,
                url = UrlResolver.resolve(bookmark.url),
                icon = FaviconCache.dataUri(context, HomePage.hostOf(bookmark.url)),
            )
        }
        val history = HistoryStore.load(context)
        val sent = HistoryStore.sent(context)
            ?.takeIf { (_, at) -> History.isSentRecent(at, now) }
            ?.let { (url, at) ->
                val title = history.firstOrNull { it.url == url }?.title ?: HomePage.hostOf(url)
                SentLink(title, url, History.ageLabel(at, now))
            }
        val recents = history
            .filter { it.url != sent?.url }
            .take(RECENT_ROWS)
            .map {
                RecentLink(
                    title = it.title,
                    url = it.url,
                    label = History.whenLabel(it.at, now),
                    icon = FaviconCache.dataUri(context, HomePage.hostOf(it.url)),
                )
            }
        return HomePage.render(palette(context), tiles, recents, sent, controlsOpen = !MirrorSettings.controlsCollapsed)
    }

    /** The colours of the chosen theme as it applies right now (automatic mode follows day/night). */
    fun palette(context: Context): Palette =
        AppTheme.resolve(MirrorSettings.theme, MirrorSettings.autoTheme, isNight(context)).palette

    /** Hosts whose icons the start page shows: the favourites and the recent pages. */
    fun hosts(context: Context): List<String> =
        (BookmarkStore.load(context).map { it.url } +
            HistoryStore.load(context).take(RECENT_ROWS + 1).map { it.url })
            .map { HomePage.hostOf(it) }
            .distinct()

    /** Called for every page the car browser finishes loading. */
    fun recordVisit(context: Context, url: String, title: String) {
        if (History.isRecordable(url)) HistoryStore.record(context, title, url)
    }

    /** The car's own day/night mode (headlights) when on the car display, else the phone's. */
    private fun isNight(context: Context): Boolean =
        (context as? CarContext)?.isDarkMode
            ?: (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES)
}
