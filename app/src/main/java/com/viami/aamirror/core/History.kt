package com.viami.aamirror.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** A page opened in the car browser; [at] is epoch milliseconds. */
data class HistoryEntry(val title: String, val url: String, val at: Long)

/** The browser history behind the start page's "Recentes" and the link sent from the phone. */
object History {

    /** How many visits are kept. */
    const val MAX_ENTRIES = 50

    /** A link sent from the phone is highlighted on the start page for this long. */
    const val SENT_HIGHLIGHT_MS = 2 * 60 * 60 * 1000L

    /** Pages worth listing: real web pages, not the start page or blank and internal ones. */
    fun isRecordable(url: String?): Boolean =
        url != null &&
            (url.startsWith("http://") || url.startsWith("https://")) &&
            !HomePage.isHome(url)

    /** Adds a visit on top; visiting a page again moves it up instead of listing it twice. */
    fun add(entries: List<HistoryEntry>, entry: HistoryEntry): List<HistoryEntry> {
        if (!isRecordable(entry.url)) return entries
        val title = entry.title.trim().ifEmpty { HomePage.hostOf(entry.url) }
        return (listOf(entry.copy(title = title)) + entries.filter { it.url != entry.url })
            .take(MAX_ENTRIES)
    }

    /** "14:10" today, "Ontem" yesterday, otherwise the date. */
    fun whenLabel(at: Long, now: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val then = Instant.ofEpochMilli(at).atZone(zone)
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        return when (then.toLocalDate()) {
            today -> then.format(DateTimeFormatter.ofPattern("HH:mm"))
            today.minusDays(1) -> "Ontem"
            else -> then.format(DateTimeFormatter.ofPattern("dd/MM"))
        }
    }

    /** "agora", "há 5 min", "há 1 h". */
    fun ageLabel(at: Long, now: Long): String {
        val minutes = ((now - at).coerceAtLeast(0) / 60_000).toInt()
        return when {
            minutes < 1 -> "agora"
            minutes < 60 -> "há $minutes min"
            else -> "há ${minutes / 60} h"
        }
    }

    fun isSentRecent(sentAt: Long, now: Long): Boolean =
        sentAt in (now - SENT_HIGHLIGHT_MS)..now

    /** One visit per line: "millis<TAB>title<TAB>url", with tabs and newlines flattened. */
    fun encode(entries: List<HistoryEntry>): String =
        entries.joinToString("\n") { "${it.at}\t${flatten(it.title)}\t${flatten(it.url)}" }

    fun decode(raw: String): List<HistoryEntry> =
        raw.lineSequence()
            .mapNotNull { line ->
                val parts = line.split('\t')
                if (parts.size != 3) return@mapNotNull null
                val at = parts[0].toLongOrNull() ?: return@mapNotNull null
                val url = parts[2].trim()
                if (url.isEmpty()) null else HistoryEntry(parts[1].trim(), url, at)
            }
            .toList()

    private fun flatten(text: String): String =
        text.replace(Regex("[\\t\\n\\r]+"), " ").trim()
}
