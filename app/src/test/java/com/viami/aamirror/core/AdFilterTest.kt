package com.viami.aamirror.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdFilterTest {

    @Test
    fun `throwaway click redirects are ads`() {
        assertTrue(AdFilter.isAdRedirect("https://groomin.space/click?key=576266f1be8d4f8bb07266f1f85a5e71&cnv_id=dbelg"))
        assertTrue(AdFilter.isAdRedirect("https://snowysnet.art/click?key=f28&zone=10522988&ssp=Adcash"))
    }

    @Test
    fun `known ad networks are ads, subdomains included`() {
        assertTrue(AdFilter.isAdRedirect("https://www.popads.net/go"))
        assertTrue(AdFilter.isAdRedirect("https://track.adcash.com/x?y=1"))
    }

    @Test
    fun `ordinary pages are not ads`() {
        assertFalse(AdFilter.isAdRedirect("https://www.youtube.com/watch?v=1"))
        assertFalse(AdFilter.isAdRedirect("https://g1.globo.com/click"))
        assertFalse(AdFilter.isAdRedirect("https://example.com/click/news?id=3"))
        assertFalse(AdFilter.isAdRedirect("https://notadcash.com/"))
        assertFalse(AdFilter.isAdRedirect(null))
    }
}
