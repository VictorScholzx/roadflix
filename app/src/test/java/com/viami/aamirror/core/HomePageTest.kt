package com.viami.aamirror.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePageTest {

    private val palette = AppTheme.MIDNIGHT.palette

    @Test
    fun `host drops scheme, www, path, query and port`() {
        assertEquals("g1.globo.com", HomePage.hostOf("https://www.g1.globo.com/economia?x=1"))
        assertEquals("youtube.com", HomePage.hostOf("www.youtube.com"))
        assertEquals("localhost", HomePage.hostOf("http://localhost:8080/"))
        assertEquals("pt.wikipedia.org", HomePage.hostOf(" PT.Wikipedia.org "))
    }

    @Test
    fun `initial is the first letter in upper case`() {
        assertEquals("G", HomePage.initialOf("google"))
        assertEquals("?", HomePage.initialOf("  "))
    }

    @Test
    fun `colour hex ignores the alpha byte`() {
        assertEquals("#4CB3F0", HomePage.hex(0xFF4CB3F0.toInt()))
        assertEquals("#000000", HomePage.hex(0xFF000000.toInt()))
    }

    @Test
    fun `tiles link to their url and show the icon or the initial`() {
        val html = HomePage.render(
            palette,
            listOf(
                HomeTile("YouTube", "https://www.youtube.com", "data:image/png;base64,AAA"),
                HomeTile("Clima", "https://climatempo.com.br", null),
            ),
        )
        assertTrue(html.contains("href=\"https://www.youtube.com\""))
        assertTrue(html.contains("src=\"data:image/png;base64,AAA\""))
        assertTrue(html.contains("<span class=\"icon letter\">C</span>"))
        assertTrue(html.contains("${HomePage.BRIDGE}.search()"))
    }

    @Test
    fun `titles are escaped`() {
        val html = HomePage.render(palette, listOf(HomeTile("<b>Tom & Jerry</b>", "https://a.b", null)))
        assertTrue(html.contains("&lt;b&gt;Tom &amp; Jerry&lt;/b&gt;"))
        assertFalse(html.contains("<b>Tom"))
    }

    @Test
    fun `no favourites shows a hint instead of an empty grid`() {
        val html = HomePage.render(palette, emptyList())
        assertTrue(html.contains("Adicione favoritos no celular"))
        assertFalse(html.contains("class=\"grid\""))
    }

    @Test
    fun `the page is painted with the theme colours`() {
        val html = HomePage.render(AppTheme.DAY.palette, emptyList())
        assertTrue(html.contains("background:#F2F2F7"))
    }

    @Test
    fun `home is recognised by its base url`() {
        assertTrue(HomePage.isHome(HomePage.URL))
        assertFalse(HomePage.isHome("https://www.google.com"))
        assertFalse(HomePage.isHome(null))
    }
}
