package com.viami.aamirror.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuPageTest {

    @Test
    fun `app links name their target`() {
        assertEquals("browser", MenuPage.target("roadflix://browser"))
        assertEquals("mirror", MenuPage.target("roadflix://mirror/"))
        assertNull(MenuPage.target("https://roadflix.menu/"))
        assertNull(MenuPage.target("roadflix://"))
        assertNull(MenuPage.target(null))
    }

    @Test
    fun `the menu has the three modes as links`() {
        val html = MenuPage.render(AppTheme.MIDNIGHT.palette)
        listOf("mirror", "browser", "youtube").forEach {
            assertTrue(html.contains("href=\"roadflix://$it\""))
        }
        assertTrue(html.contains("Espelhar tela"))
        assertTrue(html.contains("Navegador"))
        assertTrue(html.contains("YouTube TV"))
    }

    @Test
    fun `the menu is painted with the theme`() {
        val html = MenuPage.render(AppTheme.AMBER.palette)
        assertTrue(html.contains("background:#0E0D0C"))
    }
}
