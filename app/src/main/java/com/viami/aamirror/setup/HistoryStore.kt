package com.viami.aamirror.setup

import android.content.Context
import com.viami.aamirror.core.History
import com.viami.aamirror.core.HistoryEntry

/** SharedPreferences-backed browser history and the last link sent from the phone; never leaves the phone. */
object HistoryStore {

    private const val PREFS = "history"
    private const val KEY_LIST = "list"
    private const val KEY_SENT_URL = "sentUrl"
    private const val KEY_SENT_AT = "sentAt"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): List<HistoryEntry> =
        History.decode(prefs(context).getString(KEY_LIST, null) ?: "")

    @Synchronized
    fun record(context: Context, title: String, url: String) {
        val updated = History.add(load(context), HistoryEntry(title, url, System.currentTimeMillis()))
        prefs(context).edit().putString(KEY_LIST, History.encode(updated)).apply()
    }

    /** Remembers a link the phone (or a passenger's phone) sent to the car. */
    fun markSent(context: Context, url: String) {
        prefs(context).edit()
            .putString(KEY_SENT_URL, url)
            .putLong(KEY_SENT_AT, System.currentTimeMillis())
            .apply()
    }

    /** The sent link and when it arrived, or null if none. */
    fun sent(context: Context): Pair<String, Long>? {
        val p = prefs(context)
        val url = p.getString(KEY_SENT_URL, null) ?: return null
        return url to p.getLong(KEY_SENT_AT, 0)
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
