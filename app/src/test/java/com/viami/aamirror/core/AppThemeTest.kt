package com.viami.aamirror.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeTest {

    @Test
    fun `every theme round-trips through its key`() {
        AppTheme.entries.forEach { assertEquals(it, AppTheme.fromKey(it.key)) }
    }

    @Test
    fun `unknown or missing key falls back to midnight`() {
        assertEquals(AppTheme.MIDNIGHT, AppTheme.fromKey(null))
        assertEquals(AppTheme.MIDNIGHT, AppTheme.fromKey("neon"))
    }

    @Test
    fun `manual mode keeps the chosen theme day and night`() {
        assertEquals(AppTheme.AMBER, AppTheme.resolve(AppTheme.AMBER, automatic = false, night = false))
        assertEquals(AppTheme.DAY, AppTheme.resolve(AppTheme.DAY, automatic = false, night = true))
    }

    @Test
    fun `automatic mode is light by day and the chosen theme by night`() {
        assertEquals(AppTheme.DAY, AppTheme.resolve(AppTheme.FOREST, automatic = true, night = false))
        assertEquals(AppTheme.FOREST, AppTheme.resolve(AppTheme.FOREST, automatic = true, night = true))
    }

    @Test
    fun `automatic mode never stays light at night`() {
        assertEquals(AppTheme.MIDNIGHT, AppTheme.resolve(AppTheme.DAY, automatic = true, night = true))
    }

    @Test
    fun `only the day theme is light`() {
        assertTrue(AppTheme.DAY.palette.isLight)
        AppTheme.entries.filter { it != AppTheme.DAY }.forEach { assertFalse(it.palette.isLight) }
    }
}
