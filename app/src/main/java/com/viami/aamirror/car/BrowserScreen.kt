package com.viami.aamirror.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.viami.aamirror.R
import com.viami.aamirror.browser.BrowserDisplay
import com.viami.aamirror.core.UrlResolver

/**
 * The browser on the car display. Search lives on the start page itself (its
 * pill opens the car keyboard) and back/forward/home on the browser's own
 * foldable pill, so the Android Auto strip only holds the menu button.
 */
class BrowserScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        SurfaceRouter.setSink(WebSink.of(carContext, BrowserDisplay))
        BrowserDisplay.onSearchRequest = { openSearch() }
        // Favourites or the theme may have changed on the phone in the meantime.
        BrowserDisplay.refreshHome()
    }

    override fun onStop(owner: LifecycleOwner) {
        BrowserDisplay.onSearchRequest = null
    }

    override fun onGetTemplate(): Template {
        // Back, forward and home are drawn by the browser itself as a foldable pill; Android Auto
        // requires a strip on this template, so it keeps just the way back to the RoadFlix menu.
        val strip = ActionStrip.Builder()
            .addAction(action(R.drawable.ic_menu) { screenManager.pop() })
            .build()
        return NavigationTemplate.Builder().setActionStrip(strip).withPan().build()
    }

    private fun openSearch() {
        screenManager.push(
            SearchScreen(carContext) { query ->
                BrowserDisplay.loadUrl(UrlResolver.resolve(query))
            }
        )
    }

    private fun action(iconRes: Int, onClick: () -> Unit): Action =
        Action.Builder()
            .setIcon(CarIcons.of(carContext, iconRes))
            .setOnClickListener(onClick)
            .build()
}
