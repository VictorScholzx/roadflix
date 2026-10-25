package com.viami.aamirror.core

/** The colours one theme paints with, as ARGB ints. */
data class Palette(
    val ground: Int,
    val surface: Int,
    val raised: Int,
    val text: Int,
    val muted: Int,
    val accent: Int,
    val button: Int,
    val buttonText: Int,
    val switchOn: Int,
    val switchOff: Int,
) {
    /** Light themes need dark system bar icons. */
    val isLight: Boolean get() = luminance(ground) > 0.5

    private fun luminance(color: Int): Double {
        val r = (color shr 16 and 0xFF) / 255.0
        val g = (color shr 8 and 0xFF) / 255.0
        val b = (color and 0xFF) / 255.0
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
}

/** The themes the user can pick on the phone; the car browser start page follows the same choice. */
enum class AppTheme(val key: String, val label: String, val palette: Palette) {
    MIDNIGHT(
        "midnight", "Meia-noite",
        Palette(
            ground = 0xFF000000.toInt(), surface = 0xFF111111.toInt(), raised = 0xFF2A2A2A.toInt(),
            text = 0xFFFFFFFF.toInt(), muted = 0xFFB4B4B4.toInt(), accent = 0xFF4CB3F0.toInt(),
            button = 0xFFFFFFFF.toInt(), buttonText = 0xFF000000.toInt(),
            switchOn = 0xFF30D158.toInt(), switchOff = 0xFF444444.toInt(),
        ),
    ),
    GRAPHITE(
        "graphite", "Grafite azul",
        Palette(
            ground = 0xFF0C0D0F.toInt(), surface = 0xFF15171A.toInt(), raised = 0xFF24272C.toInt(),
            text = 0xFFF2F4F7.toInt(), muted = 0xFFA9B1BC.toInt(), accent = 0xFF6CB8FF.toInt(),
            button = 0xFF6CB8FF.toInt(), buttonText = 0xFF06121F.toInt(),
            switchOn = 0xFF6CB8FF.toInt(), switchOff = 0xFF3A3F47.toInt(),
        ),
    ),
    AMBER(
        "amber", "Âmbar noturno",
        Palette(
            ground = 0xFF0E0D0C.toInt(), surface = 0xFF181716.toInt(), raised = 0xFF2A2725.toInt(),
            text = 0xFFECE8E1.toInt(), muted = 0xFFBDB6AB.toInt(), accent = 0xFFF2B33D.toInt(),
            button = 0xFFF2B33D.toInt(), buttonText = 0xFF1A1205.toInt(),
            switchOn = 0xFFF2B33D.toInt(), switchOff = 0xFF45403B.toInt(),
        ),
    ),
    FOREST(
        "forest", "Floresta",
        Palette(
            ground = 0xFF0B0F0D.toInt(), surface = 0xFF131A16.toInt(), raised = 0xFF222C26.toInt(),
            text = 0xFFEEF3EF.toInt(), muted = 0xFFA8B5AD.toInt(), accent = 0xFF4FD18B.toInt(),
            button = 0xFFEEF3EF.toInt(), buttonText = 0xFF0B0F0D.toInt(),
            switchOn = 0xFF4FD18B.toInt(), switchOff = 0xFF36423B.toInt(),
        ),
    ),
    CORAL(
        "coral", "Coral esportivo",
        Palette(
            ground = 0xFF0D0B0B.toInt(), surface = 0xFF191414.toInt(), raised = 0xFF2C2424.toInt(),
            text = 0xFFF7F1F0.toInt(), muted = 0xFFBFB1AF.toInt(), accent = 0xFFFF6B5A.toInt(),
            button = 0xFFFF6B5A.toInt(), buttonText = 0xFF1A0806.toInt(),
            switchOn = 0xFFFF6B5A.toInt(), switchOff = 0xFF463A3A.toInt(),
        ),
    ),
    DAY(
        "day", "Dia (claro)",
        Palette(
            ground = 0xFFF2F2F7.toInt(), surface = 0xFFFFFFFF.toInt(), raised = 0xFFE5E5EA.toInt(),
            text = 0xFF000000.toInt(), muted = 0xFF5F6368.toInt(), accent = 0xFF007AFF.toInt(),
            button = 0xFF000000.toInt(), buttonText = 0xFFFFFFFF.toInt(),
            switchOn = 0xFF34C759.toInt(), switchOff = 0xFFD1D1D6.toInt(),
        ),
    );

    companion object {
        val DEFAULT = MIDNIGHT

        fun fromKey(key: String?): AppTheme = entries.firstOrNull { it.key == key } ?: DEFAULT

        /**
         * The theme to paint with. In automatic mode daytime is always [DAY] and night is the
         * chosen theme, or [MIDNIGHT] when the chosen one is [DAY] itself.
         */
        fun resolve(chosen: AppTheme, automatic: Boolean, night: Boolean): AppTheme = when {
            !automatic -> chosen
            !night -> DAY
            chosen == DAY -> MIDNIGHT
            else -> chosen
        }
    }
}
