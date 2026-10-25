package com.viami.aamirror.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class HistoryTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    private fun at(day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(2026, 10, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `new visits go on top and repeats move up`() {
        val a = HistoryEntry("A", "https://a.com", 1)
        val b = HistoryEntry("B", "https://b.com", 2)
        val list = History.add(History.add(emptyList(), a), b)
        assertEquals(listOf("https://b.com", "https://a.com"), list.map { it.url })
        val again = History.add(list, a.copy(at = 3))
        assertEquals(listOf("https://a.com", "https://b.com"), again.map { it.url })
    }

    @Test
    fun `start page and non-web urls are not recorded`() {
        assertTrue(History.add(emptyList(), HistoryEntry("Início", HomePage.URL, 1)).isEmpty())
        assertTrue(History.add(emptyList(), HistoryEntry("", "about:blank", 1)).isEmpty())
        assertTrue(History.add(emptyList(), HistoryEntry("", "data:text/html,x", 1)).isEmpty())
    }

    @Test
    fun `ad redirects are not recorded`() {
        assertTrue(History.add(emptyList(), HistoryEntry("Atenção!", "https://groomin.space/click?key=5", 1)).isEmpty())
    }

    @Test
    fun `remove drops only that page`() {
        val list = listOf(HistoryEntry("A", "https://a.com", 2), HistoryEntry("B", "https://b.com", 1))
        assertEquals(listOf("https://b.com"), History.remove(list, "https://a.com").map { it.url })
    }

    @Test
    fun `a page with no title is listed by its host`() {
        val list = History.add(emptyList(), HistoryEntry("  ", "https://www.g1.globo.com/x", 1))
        assertEquals("g1.globo.com", list.single().title)
    }

    @Test
    fun `history is capped`() {
        var list = emptyList<HistoryEntry>()
        repeat(History.MAX_ENTRIES + 10) { list = History.add(list, HistoryEntry("p$it", "https://p$it.com", it.toLong())) }
        assertEquals(History.MAX_ENTRIES, list.size)
        assertEquals("https://p${History.MAX_ENTRIES + 9}.com", list.first().url)
    }

    @Test
    fun `when label says the time today, ontem yesterday and the date before`() {
        val now = at(24, 21, 0)
        assertEquals("14:10", History.whenLabel(at(24, 14, 10), now, zone))
        assertEquals("Ontem", History.whenLabel(at(23, 23, 59), now, zone))
        assertEquals("20/10", History.whenLabel(at(20, 9, 0), now, zone))
    }

    @Test
    fun `age label counts minutes then hours`() {
        val now = at(24, 21, 0)
        assertEquals("agora", History.ageLabel(now - 20_000, now))
        assertEquals("há 2 min", History.ageLabel(now - 2 * 60_000, now))
        assertEquals("há 1 h", History.ageLabel(now - 75 * 60_000, now))
    }

    @Test
    fun `a sent link is highlighted for two hours`() {
        val now = at(24, 21, 0)
        assertTrue(History.isSentRecent(now - 60_000, now))
        assertFalse(History.isSentRecent(now - History.SENT_HIGHLIGHT_MS - 1, now))
        assertFalse(History.isSentRecent(0, now))
    }

    @Test
    fun `codec round-trips and skips broken lines`() {
        val list = listOf(HistoryEntry("A\tB", "https://a.com", 5), HistoryEntry("C", "https://c.com", 6))
        val decoded = History.decode(History.encode(list))
        assertEquals(listOf(HistoryEntry("A B", "https://a.com", 5), HistoryEntry("C", "https://c.com", 6)), decoded)
        assertEquals(emptyList<HistoryEntry>(), History.decode("x\ty\n\t\t\nabc\tT\thttps://x"))
    }
}
