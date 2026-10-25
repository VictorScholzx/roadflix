package com.viami.aamirror.setup

import android.content.Context
import com.viami.aamirror.core.AppTheme
import com.viami.aamirror.core.MenuLayout
import com.viami.aamirror.core.MirrorSettings

/**
 * SharedPreferences behind [MirrorSettings], which stays a plain in-memory
 * holder so the renderers can read it without a Context.
 */
object SettingsStore {

    private const val PREFS = "settings"
    private const val KEY_FILL = "fillScreen"
    private const val KEY_LAYOUT = "menuLayout"
    private const val KEY_THEME = "theme"
    private const val KEY_AUTO_THEME = "autoTheme"

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        MirrorSettings.fillScreen = prefs.getBoolean(KEY_FILL, false)
        MirrorSettings.menuLayout = MenuLayout.fromKey(prefs.getString(KEY_LAYOUT, null))
        MirrorSettings.theme = AppTheme.fromKey(prefs.getString(KEY_THEME, null))
        MirrorSettings.autoTheme = prefs.getBoolean(KEY_AUTO_THEME, false)
    }

    fun save(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FILL, MirrorSettings.fillScreen)
            .putString(KEY_LAYOUT, MirrorSettings.menuLayout.key)
            .putString(KEY_THEME, MirrorSettings.theme.key)
            .putBoolean(KEY_AUTO_THEME, MirrorSettings.autoTheme)
            .apply()
    }
}
