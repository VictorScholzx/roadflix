package com.viami.aamirror.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.viami.aamirror.R
import com.viami.aamirror.browser.BrowserDisplay
import com.viami.aamirror.browser.MenuDisplay
import com.viami.aamirror.setup.SettingsStore

/**
 * Root menu of RoadFlix. The menu is drawn by the app on the car surface (see
 * MenuPage) instead of Android Auto's grid, so it follows the design and theme.
 * The template is always a NavigationTemplate, so coming back to it never
 * changes template type, which the host rejects.
 */
class HomeScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        SurfaceRouter.register(carContext)
        SettingsStore.load(carContext)
    }

    override fun onStart(owner: LifecycleOwner) {
        MenuDisplay.onAppLink = { target -> open(target) }
        SurfaceRouter.setSink(WebSink.of(carContext, MenuDisplay))
        // The theme may have changed on the phone in the meantime.
        MenuDisplay.refreshHome()
    }

    override fun onStop(owner: LifecycleOwner) {
        MenuDisplay.onAppLink = null
    }

    private fun open(target: String) {
        when (target) {
            "mirror" -> screenManager.push(MirrorScreen(carContext))
            "browser" -> {
                BrowserDisplay.goHome()
                screenManager.push(BrowserScreen(carContext))
            }
            "youtube" -> screenManager.push(YouTubeScreen(carContext))
        }
    }

    override fun onGetTemplate(): Template {
        val strip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setIcon(icon(R.drawable.ic_settings))
                    .setOnClickListener { screenManager.push(SettingsScreen(carContext)) }
                    .build()
            )
            .build()
        return NavigationTemplate.Builder().setActionStrip(strip).build()
    }

    private fun icon(iconRes: Int): CarIcon =
        CarIcons.of(carContext, iconRes)
}
