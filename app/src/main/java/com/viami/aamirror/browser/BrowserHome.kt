package com.viami.aamirror.browser

import android.content.Context
import android.content.res.Configuration
import androidx.car.app.CarContext
import com.viami.aamirror.core.AppTheme
import com.viami.aamirror.core.HomePage
import com.viami.aamirror.core.HomeTile
import com.viami.aamirror.core.MirrorSettings
import com.viami.aamirror.core.UrlResolver
import com.viami.aamirror.setup.BookmarkStore

/** Builds the start page from the saved favourites, their cached icons and the chosen theme. */
object BrowserHome {

    fun html(context: Context): String {
        val tiles = BookmarkStore.load(context).map { bookmark ->
            HomeTile(
                title = bookmark.title,
                url = UrlResolver.resolve(bookmark.url),
                icon = FaviconCache.dataUri(context, HomePage.hostOf(bookmark.url)),
            )
        }
        val theme = AppTheme.resolve(MirrorSettings.theme, MirrorSettings.autoTheme, isNight(context))
        return HomePage.render(theme.palette, tiles)
    }

    fun hosts(context: Context): List<String> =
        BookmarkStore.load(context).map { HomePage.hostOf(it.url) }

    /** The car's own day/night mode (headlights) when on the car display, else the phone's. */
    private fun isNight(context: Context): Boolean =
        (context as? CarContext)?.isDarkMode
            ?: (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES)
}
