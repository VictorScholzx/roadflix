package com.viami.aamirror.core

/** Mode flags, toggled from the car settings screen. */
object MirrorSettings {
    /** true = center-crop the phone screen so it fills the whole car display. */
    @Volatile
    var fillScreen: Boolean = false

    /** How the car home screen lists the modes. */
    @Volatile
    var menuLayout: MenuLayout = MenuLayout.GRID

    /** The theme picked on the phone. */
    @Volatile
    var theme: AppTheme = AppTheme.DEFAULT

    /** true = light by day, [theme] by night. */
    @Volatile
    var autoTheme: Boolean = false
}
