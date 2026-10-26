package com.viami.aamirror.core

/** Mode flags, toggled from the car settings screen. */
object MirrorSettings {
    /** true = center-crop the phone screen so it fills the whole car display. */
    @Volatile
    var fillScreen: Boolean = false

    /** The theme picked on the phone. */
    @Volatile
    var theme: AppTheme = AppTheme.DEFAULT

    /** true = light by day, [theme] by night. */
    @Volatile
    var autoTheme: Boolean = false

    /** true = the browser's back/forward/home pill is folded down to its handle; it opens folded. */
    @Volatile
    var controlsCollapsed: Boolean = true
}
