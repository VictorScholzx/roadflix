package com.viami.aamirror.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabListTest {

    private val home = HomePage.URL

    private fun list(vararg urls: String, active: Int = 0) =
        TabList(urls.map { TabInfo(it) }, active)

    @Test
    fun `opening a tab adds it at the end and shows it`() {
        val tabs = TabList.single(home).open("https://a.com")
        assertEquals(2, tabs.size)
        assertEquals(1, tabs.active)
        assertEquals("https://a.com", tabs.current.url)
    }

    @Test
    fun `no more tabs open past the limit`() {
        var tabs = TabList.single(home)
        repeat(10) { tabs = tabs.open(home) }
        assertEquals(TabList.MAX_TABS, tabs.size)
        assertFalse(tabs.canOpen)
    }

    @Test
    fun `closing the shown tab shows its left neighbour`() {
        val tabs = list("a", "b", "c", active = 2).close(2, home)
        assertEquals(listOf("a", "b"), tabs.tabs.map { it.url })
        assertEquals(1, tabs.active)
    }

    @Test
    fun `closing the first shown tab shows the next one`() {
        val tabs = list("a", "b", active = 0).close(0, home)
        assertEquals("b", tabs.current.url)
        assertEquals(0, tabs.active)
    }

    @Test
    fun `closing a tab before the shown one keeps showing the same page`() {
        val tabs = list("a", "b", "c", active = 2).close(0, home)
        assertEquals("c", tabs.current.url)
    }

    @Test
    fun `closing the only tab leaves a fresh start page`() {
        val tabs = list("https://a.com").close(0, home)
        assertEquals(TabList.single(home), tabs)
    }

    @Test
    fun `labels say inicio, the title or the site`() {
        val tabs = TabList(
            listOf(TabInfo(home), TabInfo("https://www.g1.globo.com/x", "G1 Notícias"), TabInfo("https://m.youtube.com/")),
            0,
        )
        assertEquals("Início", tabs.label(0))
        assertEquals("G1 Notícias", tabs.label(1))
        assertEquals("m.youtube.com", tabs.label(2))
    }

    @Test
    fun `update and select ignore indexes out of range`() {
        val tabs = list("a")
        assertEquals(tabs, tabs.select(3))
        assertEquals(tabs, tabs.update(-1, "x", "y"))
        assertTrue(tabs.update(0, "x", "y").current == TabInfo("x", "y"))
    }
}
