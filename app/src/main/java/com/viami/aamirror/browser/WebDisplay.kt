package com.viami.aamirror.browser

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.viami.aamirror.R
import com.viami.aamirror.core.AdFilter
import com.viami.aamirror.core.ArrowKey
import com.viami.aamirror.core.GesturePoint
import com.viami.aamirror.core.HomePage
import com.viami.aamirror.core.KeyScroll
import com.viami.aamirror.core.MenuPage
import com.viami.aamirror.core.MirrorSettings
import com.viami.aamirror.core.Palette
import com.viami.aamirror.core.ScrollGesture
import com.viami.aamirror.core.TabList
import com.viami.aamirror.core.WebViewport
import com.viami.aamirror.setup.HistoryStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/** Smart-TV user agent so youtube.com/tv serves the leanback interface. */
const val TV_USER_AGENT =
    "Mozilla/5.0 (SMART-TV; Linux; Tizen 5.5) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Version/5.5 TV Safari/537.36"

/** The plain web browser mode; its home is the local start page with the favourites. */
val BrowserDisplay: WebDisplay = WebDisplay(
    homeUrl = HomePage.URL,
    homePage = BrowserHome::html,
    onVisit = BrowserHome::recordVisit,
    blockAdRedirects = true,
    tabPalette = BrowserHome::palette,
)

/** RoadFlix's own main menu, drawn on the car surface in the chosen theme. */
val MenuDisplay: WebDisplay = WebDisplay(
    homeUrl = MenuPage.URL,
    homePage = { context -> MenuPage.render(BrowserHome.palette(context)) },
)

/**
 * The YouTube "TV" mode, cast target for the second phone. The TV interface
 * is built for a 1280-wide screen; the car surface is half that, so the
 * WebView zooms out until the page has that much room to lay itself out in.
 * Its leanback UI moves focus with a remote and ignores touch drags, so car
 * scrolling is turned into arrow-key presses.
 */
val YouTubeDisplay: WebDisplay = WebDisplay(
    homeUrl = "https://www.youtube.com/tv",
    userAgent = TV_USER_AGENT,
    logicalWidth = 1280,
    scrollWithKeys = true,
)

/**
 * Renders a WebView straight onto the car screen: the car Surface backs a
 * private VirtualDisplay, a Presentation shows the WebView on it, and car
 * taps/scrolls are injected into the view hierarchy as MotionEvents.
 */
class WebDisplay(
    private val homeUrl: String,
    private val userAgent: String? = null,
    private val logicalWidth: Int? = null,
    private val scrollWithKeys: Boolean = false,
    private val homePage: ((Context) -> String)? = null,
    private val onVisit: ((Context, String, String) -> Unit)? = null,
    private val blockAdRedirects: Boolean = false,
    /** Set for the browser: shows a tab bar painted with this palette. */
    private val tabPalette: ((Context) -> Palette)? = null,
) {

    /** Called when the start page's search pill is tapped; the car screen opens its keyboard. */
    var onSearchRequest: (() -> Unit)? = null

    /** Called with the target of a tapped roadflix:// link ("browser", "mirror", ...). */
    var onAppLink: ((String) -> Unit)? = null


    private val scope = MainScope()
    private var appContext: Context? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null

    /** Survives detach/attach cycles so the tabs come back after focus loss. */
    private var tabs: TabList = TabList.single(homeUrl)

    /** One WebView per tab, in the same order as [tabs]; empty while detached. */
    private val views = mutableListOf<WebView>()
    private var container: FrameLayout? = null
    private var tabBar: LinearLayout? = null
    private var controls: LinearLayout? = null
    private var scalePercent = 100

    private val webView: WebView?
        get() = views.getOrNull(tabs.active)

    /** State of the drag the car scroll events are being folded into. */
    private var gestureActive = false
    private var fingerPoint = GesturePoint(0f, 0f)
    private var gestureDownTime = 0L
    private val liftFinger = Runnable { finishGesture() }

    /** Folds car scroll events into arrow keys when [scrollWithKeys] is set. */
    private val keyScroll = KeyScroll()
    private val resetKeyScroll = Runnable { keyScroll.reset() }

    val isAttached: Boolean
        get() = presentation != null

    fun attach(context: Context, surface: Surface, width: Int, height: Int, densityDpi: Int) {
        mainHandler.post {
            detachNow()
            // The host paints the virtual display pixel for pixel, so its
            // resolution must match the surface: a bigger one only crops.
            scalePercent = logicalWidth
                ?.let { WebViewport.initialScalePercent(width, it) }
                ?: 100
            Log.i(TAG, "attach surface=${width}x$height dpi=$densityDpi scale=$scalePercent%")
            val manager = context.getSystemService(DisplayManager::class.java)
            val display = manager.createVirtualDisplay(
                "AAWeb",
                width,
                height,
                densityDpi,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
            )
            val shown = Presentation(context, display.display)
            appContext = context
            // The control pill always starts folded away.
            MirrorSettings.controlsCollapsed = true
            val pages = FrameLayout(shown.context)
            container = pages
            if (tabPalette != null) {
                val root = LinearLayout(shown.context).apply { orientation = LinearLayout.VERTICAL }
                val bar = LinearLayout(shown.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }
                tabBar = bar
                root.addView(bar, LinearLayout.LayoutParams(MATCH_PARENT, dp(shown.context, 60)))
                root.addView(pages, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
                // Back, forward and home float as a vertical pill on the right, drawn by the app
                // so it can be folded away. It hangs right under Android Auto's own ≡ button
                // (which the host draws in the top right corner and cannot be moved), lined up
                // with it and the same size, so the two read as one column.
                val pill = LinearLayout(shown.context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                controls = pill
                val frame = FrameLayout(shown.context)
                frame.addView(root, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                frame.addView(
                    pill,
                    FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.END or Gravity.TOP),
                )
                shown.setContentView(frame)
            } else {
                shown.setContentView(pages)
            }
            tabs.tabs.forEach { tab -> addView(shown.context, tab.url) }
            showActive()
            shown.show()
            if (tabPalette != null) fetchIcons(context)
            virtualDisplay = display
            presentation = shown
            renderTabBar()
            // Android Auto shows its ≡ when the screen opens; the handle comes up with it.
            wakeControls()
        }
    }

    fun detach() {
        mainHandler.post { detachNow() }
    }

    private fun detachNow() {
        cancelGesture()
        mainHandler.removeCallbacks(fadeControls)
        presentation?.dismiss()
        views.forEach { it.destroy() }
        views.clear()
        virtualDisplay?.release()
        presentation = null
        virtualDisplay = null
        container = null
        tabBar = null
        controls = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun addView(context: Context, url: String): WebView {
        val web = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            userAgent?.let { settings.userAgentString = it }
            if (scalePercent != 100) {
                // Zoom out so the page lays itself out for a wide screen
                // while still being drawn on the small car surface.
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                setInitialScale(scalePercent)
            }
            webViewClient = object : WebViewClient() {
                /** Ad-network redirects ("Atenção!" pages) are dropped instead of opened. */
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    MenuPage.target(request.url.toString())?.let { target ->
                        mainHandler.post { onAppLink?.invoke(target) }
                        return true
                    }
                    val blocked = blockAdRedirects && request.isForMainFrame &&
                        AdFilter.isAdRedirect(request.url.toString())
                    if (blocked) Log.i(TAG, "blocked ad redirect ${request.url.host}")
                    return blocked
                }

                override fun onPageFinished(view: WebView, url: String) {
                    val index = views.indexOf(view)
                    // A generated page reports a "data:" URL; the tab keeps its real address.
                    val shown = if (url.startsWith("data:") && showsHome(index)) tabs.tabs[index].url else url
                    if (index >= 0) tabs = tabs.update(index, shown, view.title.orEmpty())
                    applyVideoFit(view)
                    appContext?.let { onVisit?.invoke(it, url, view.title.orEmpty()) }
                    renderTabBar()
                }
            }
        }
        if (homePage != null) web.addJavascriptInterface(Bridge(), HomePage.BRIDGE)
        container?.addView(web, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        views.add(web)
        loadInto(web, url)
        return web
    }

    /** Only the showing tab is visible; the others keep their pages loaded behind it. */
    private fun showActive() {
        views.forEachIndexed { i, web -> web.visibility = if (i == tabs.active) View.VISIBLE else View.GONE }
    }

    fun loadUrl(url: String) {
        mainHandler.post {
            tabs = tabs.update(tabs.active, url, "")
            webView?.let { loadInto(it, url) }
            renderTabBar()
        }
    }

    /** The start page is generated locally; everything else is a normal load. */
    private fun loadInto(web: WebView, url: String) {
        val context = appContext
        if (homePage != null && context != null && isHomeUrl(url)) {
            web.loadDataWithBaseURL(homeUrl, homePage.invoke(context), "text/html", "utf-8", homeUrl)
        } else {
            web.loadUrl(url)
        }
    }

    /** A new tab on the start page, shown at once. */
    fun newTab() {
        mainHandler.post {
            val shown = presentation ?: return@post
            if (!tabs.canOpen) return@post
            finishGesture()
            tabs = tabs.open(homeUrl)
            addView(shown.context, homeUrl)
            showActive()
            renderTabBar()
        }
    }

    fun selectTab(index: Int) {
        mainHandler.post {
            finishGesture()
            tabs = tabs.select(index)
            showActive()
            renderTabBar()
        }
    }

    fun closeTab(index: Int) {
        mainHandler.post {
            if (index !in views.indices) return@post
            finishGesture()
            val closingLast = tabs.size == 1
            tabs = tabs.close(index, homeUrl)
            if (closingLast) {
                // The only tab is never removed, just sent back to the start page.
                webView?.let { it.clearHistory(); loadInto(it, homeUrl) }
            } else {
                val web = views.removeAt(index)
                container?.removeView(web)
                web.destroy()
                showActive()
            }
            renderTabBar()
        }
    }

    /** Redraws the start page wherever it is showing, after a theme change or new icons. */
    fun refreshHome() {
        mainHandler.post {
            views.forEachIndexed { i, web -> if (showsHome(i)) loadInto(web, homeUrl) }
            renderTabBar()
            // A favourite added on the phone gets its icon on the next refresh.
            if (tabPalette != null) appContext?.let { fetchIcons(it) }
        }
    }

    private fun fetchIcons(context: Context) {
        scope.launch {
            if (FaviconCache.fetchMissing(context, BrowserHome.hosts(context))) refreshHome()
        }
    }

    /** The page this display generates itself (start page or menu) lives under [homeUrl]. */
    private fun isHomeUrl(url: String?): Boolean = homePage != null && url != null && url.startsWith(homeUrl)

    /**
     * Whether tab [index] shows the generated page. Asked of the tab list, not the WebView:
     * for a page loaded from a string, WebView.getUrl() reports a "data:" URL, not its base URL.
     */
    private fun showsHome(index: Int): Boolean = isHomeUrl(tabs.tabs.getOrNull(index)?.url)

    fun goForward() {
        mainHandler.post { webView?.let { if (it.canGoForward()) it.goForward() } }
    }

    /** The chips across the top: one per tab (tap to show, × to close) and a + for a new one. */
    private fun renderTabBar() {
        val bar = tabBar ?: return
        val context = appContext ?: return
        val palette = tabPalette?.invoke(context) ?: return
        renderControls(palette)
        val c = bar.context
        bar.removeAllViews()
        bar.setBackgroundColor(palette.ground)
        // The car's action strip floats over the top right corner.
        bar.setPadding(dp(c, 12), 0, dp(c, 104), 0)
        for (i in 0 until tabs.size) {
            val selected = i == tabs.active
            val chip = LinearLayout(c).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = pill(if (selected) palette.button else palette.surface, dp(c, 22))
                setPadding(dp(c, 16), 0, dp(c, if (selected && tabs.size > 1) 4 else 16), 0)
                // Tapping the tab already showing closes it, as its × does; any other tab is shown.
                setOnClickListener { if (selected && tabs.size > 1) closeTab(i) else selectTab(i) }
            }
            chip.addView(TextView(c).apply {
                text = tabs.label(i)
                setTextColor(if (selected) palette.buttonText else palette.muted)
                textSize = 15f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                maxWidth = dp(c, 170)
            })
            if (selected && tabs.size > 1) {
                chip.addView(TextView(c).apply {
                    text = "×"
                    textSize = 20f
                    gravity = Gravity.CENTER
                    setTextColor(if (selected) palette.buttonText else palette.muted)
                    contentDescription = "Fechar aba"
                    setOnClickListener { closeTab(i) }
                }, LinearLayout.LayoutParams(dp(c, 40), dp(c, 44)))
            }
            bar.addView(chip, LinearLayout.LayoutParams(WRAP_CONTENT, dp(c, 44)).apply {
                marginEnd = dp(c, 8)
            })
        }
        bar.addView(TextView(c).apply {
            text = "+"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(if (tabs.canOpen) palette.text else palette.raised)
            background = pill(palette.surface, dp(c, 22))
            contentDescription = "Nova aba"
            isEnabled = tabs.canOpen
            setOnClickListener { newTab() }
        }, LinearLayout.LayoutParams(dp(c, 44), dp(c, 44)))
    }

    /** The vertical pill of back, forward and home, or just its handle when folded away. */
    /**
     * Shows the folded handle for as long as Android Auto shows its ≡ button after a touch
     * (about ten seconds, measured on the host), so the two appear and fade together. The open
     * pill stays until it is folded.
     */
    private fun wakeControls() {
        val pill = controls ?: return
        mainHandler.removeCallbacks(fadeControls)
        pill.animate().cancel()
        pill.visibility = View.VISIBLE
        pill.alpha = 1f
        if (MirrorSettings.controlsCollapsed) mainHandler.postDelayed(fadeControls, CONTROLS_VISIBLE_MS)
    }

    private val fadeControls = Runnable {
        val pill = controls ?: return@Runnable
        if (!MirrorSettings.controlsCollapsed) return@Runnable
        pill.animate().alpha(0f).setDuration(300).withEndAction {
            // Invisible, not just transparent, so the next tap reaches the page underneath.
            if (MirrorSettings.controlsCollapsed) pill.visibility = View.INVISIBLE
        }
    }

    private fun renderControls(palette: Palette) {
        val pill = controls ?: return
        val c = pill.context
        pill.removeAllViews()
        pill.elevation = dp(c, 6).toFloat()
        val collapsed = MirrorSettings.controlsCollapsed
        // Both shapes hang centred under Android Auto's ≡ (about 76 dp wide, its bottom edge
        // about 76 dp down on the car display): the open pill just under it, the folded tab touching it.
        val width = if (collapsed) HANDLE_WIDTH_DP else PILL_WIDTH_DP
        pill.layoutParams = (pill.layoutParams as FrameLayout.LayoutParams).apply {
            marginEnd = dp(c, PILL_MARGIN_END_DP + (PILL_WIDTH_DP - width) / 2)
            topMargin = dp(c, if (collapsed) 74 else 82)
        }
        if (collapsed) {
            // Folded: a small tab, like a pull handle attached to the ≡ button.
            pill.background = pill(palette.surface, dp(c, HANDLE_HEIGHT_DP / 2))
            pill.setPadding(0, 0, 0, 0)
            pill.addView(ImageView(c).apply {
                // Pointing down: the controls open downwards from here.
                setImageResource(R.drawable.ic_collapse)
                imageTintList = ColorStateList.valueOf(palette.text)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(0, dp(c, 2), 0, dp(c, 2))
                contentDescription = "Mostrar controles"
                setOnClickListener { toggleControls(palette) }
                layoutParams = LinearLayout.LayoutParams(dp(c, HANDLE_WIDTH_DP), dp(c, HANDLE_HEIGHT_DP))
            })
            return
        }
        pill.background = pill(palette.surface, dp(c, PILL_WIDTH_DP / 2))
        pill.setPadding(dp(c, 6), dp(c, 6), dp(c, 6), dp(c, 6))
        pill.addView(controlButton(c, palette, R.drawable.ic_back, "Voltar") { goBack() })
        pill.addView(controlButton(c, palette, R.drawable.ic_forward, "Avançar") { goForward() })
        pill.addView(controlButton(c, palette, R.drawable.ic_home, "Início") { goHome() })
        pill.addView(ImageView(c).apply {
            // Pointing up: the controls fold back up into the tab.
            setImageResource(R.drawable.ic_expand)
            imageTintList = ColorStateList.valueOf(palette.muted)
            scaleType = ImageView.ScaleType.CENTER
            contentDescription = "Esconder controles"
            setOnClickListener { toggleControls(palette) }
            layoutParams = LinearLayout.LayoutParams(dp(c, 64), dp(c, 40))
        })
    }

    private fun toggleControls(palette: Palette) {
        MirrorSettings.controlsCollapsed = !MirrorSettings.controlsCollapsed
        renderControls(palette)
        wakeControls()
        // The start page widens into the space the pill gives back, or makes room for it.
        val open = !MirrorSettings.controlsCollapsed
        views.forEachIndexed { i, web ->
            if (showsHome(i)) web.evaluateJavascript("document.body.classList.toggle('controls-open', $open)", null)
        }
    }

    private fun controlButton(c: Context, palette: Palette, iconRes: Int, description: String, onClick: () -> Unit): View =
        ImageView(c).apply {
            setImageResource(iconRes)
            imageTintList = ColorStateList.valueOf(palette.text)
            background = pill(palette.raised, dp(c, 32))
            scaleType = ImageView.ScaleType.CENTER
            contentDescription = description
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(dp(c, 64), dp(c, 64)).apply {
                topMargin = dp(c, 4)
                bottomMargin = dp(c, 4)
            }
        }

    private fun pill(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
    }

    private fun dp(context: Context, value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics)
            .toInt()

    /** What the start page's JavaScript can call; it only acts while the start page is shown. */
    private inner class Bridge {
        @JavascriptInterface
        fun search() {
            mainHandler.post {
                if (showsHome(tabs.active)) onSearchRequest?.invoke()
            }
        }

        @JavascriptInterface
        fun removeRecent(url: String) {
            mainHandler.post {
                val context = appContext ?: return@post
                if (!showsHome(tabs.active)) return@post
                HistoryStore.remove(context, url)
                refreshHome()
            }
        }

        @JavascriptInterface
        fun removeSent() {
            mainHandler.post {
                val context = appContext ?: return@post
                if (!showsHome(tabs.active)) return@post
                HistoryStore.clearSent(context)
                refreshHome()
            }
        }
    }

    /**
     * Back for both kinds of page. An ordinary site is walked back through
     * the history; a TV app keeps its own routing and leaves the history
     * empty, so it gets the remote's back key as a keyboard event injected
     * into the page — a native key event would need the WebView to hold the
     * focus, which it does not inside a Presentation.
     */
    fun goBack() {
        mainHandler.post {
            val web = webView ?: return@post
            if (web.canGoBack()) {
                web.goBack()
                return@post
            }
            web.evaluateJavascript(BACK_KEY_JS, null)
        }
    }

    fun goHome() = loadUrl(homeUrl)

    fun reload() {
        mainHandler.post { webView?.reload() }
    }

    /** Re-applies the fill/fit choice to a page that is already loaded. */
    fun refreshVideoFit() {
        mainHandler.post { webView?.let { applyVideoFit(it) } }
    }

    fun tap(x: Float, y: Float) {
        mainHandler.post {
            val target = decorView() ?: return@post
            finishGesture()
            val downTime = SystemClock.uptimeMillis()
            dispatch(target, MotionEvent.ACTION_DOWN, GesturePoint(x, y), downTime)
            dispatch(target, MotionEvent.ACTION_UP, GesturePoint(x, y), downTime)
            // After the tap is delivered, so a tap that wakes the handle does not also press it.
            wakeControls()
        }
    }

    /**
     * The host reports scrolling as a stream of small distances. Replaying
     * each one with WebView.scrollBy only moves the root document, which
     * modern pages never scroll; folding them into one real drag lets the
     * page scroll whichever container is under the finger.
     */
    fun scroll(distanceX: Float, distanceY: Float) {
        if (scrollWithKeys) {
            pressArrowKeys(distanceX, distanceY)
            return
        }
        mainHandler.post {
            val target = decorView() ?: return@post
            wakeControls()
            if (!gestureActive) beginGesture(target)
            val step = ScrollGesture.advance(
                fingerPoint, distanceX, distanceY, target.width, target.height
            )
            fingerPoint = step.point
            dispatch(target, MotionEvent.ACTION_MOVE, fingerPoint, gestureDownTime)
            mainHandler.removeCallbacks(liftFinger)
            if (step.restart) {
                // The finger reached an edge: lift it so the next event
                // starts a fresh drag from the centre.
                finishGesture()
            } else {
                mainHandler.postDelayed(liftFinger, GESTURE_IDLE_MS)
            }
        }
    }

    private fun pressArrowKeys(distanceX: Float, distanceY: Float) {
        mainHandler.post {
            val web = webView ?: return@post
            mainHandler.removeCallbacks(resetKeyScroll)
            mainHandler.postDelayed(resetKeyScroll, GESTURE_IDLE_MS)
            val keys = keyScroll.advance(distanceX, distanceY)
            if (keys.isNotEmpty()) web.evaluateJavascript(arrowKeysJs(keys), null)
        }
    }

    private fun beginGesture(target: View) {
        gestureDownTime = SystemClock.uptimeMillis()
        fingerPoint = ScrollGesture.origin(target.width, target.height)
        dispatch(target, MotionEvent.ACTION_DOWN, fingerPoint, gestureDownTime)
        gestureActive = true
    }

    private fun finishGesture() {
        if (!gestureActive) return
        gestureActive = false
        mainHandler.removeCallbacks(liftFinger)
        val target = decorView() ?: return
        dispatch(target, MotionEvent.ACTION_UP, fingerPoint, gestureDownTime)
    }

    private fun cancelGesture() {
        mainHandler.removeCallbacks(liftFinger)
        gestureActive = false
        mainHandler.removeCallbacks(resetKeyScroll)
        keyScroll.reset()
    }

    private fun decorView(): View? = presentation?.window?.decorView

    private fun dispatch(target: View, action: Int, point: GesturePoint, downTime: Long) {
        val event = MotionEvent.obtain(
            downTime, SystemClock.uptimeMillis(), action, point.x, point.y, 0
        ).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        target.dispatchTouchEvent(event)
        event.recycle()
    }

    /**
     * The car display is wider than 16:9, so a video letterboxes by default.
     * With "fill screen" on it is cropped to cover the display instead.
     */
    private fun applyVideoFit(view: WebView) {
        val css = if (MirrorSettings.fillScreen) {
            "video{object-fit:cover!important;width:100%!important;height:100%!important;}"
        } else {
            "video{object-fit:contain!important;}"
        }
        val js = "(function(){var s=document.getElementById('$FIT_STYLE_ID');" +
            "if(!s){s=document.createElement('style');s.id='$FIT_STYLE_ID';" +
            "document.head.appendChild(s);}s.textContent=\"$css\";})();"
        view.evaluateJavascript(js, null)
    }

    private companion object {
        const val TAG = "WebDisplay"
        const val FIT_STYLE_ID = "aa-video-fit"

        /**
         * The back key as the TV platforms report it: 27 is Escape (what a
         * browser-hosted leanback UI listens for), 461 is webOS and 10009 is
         * Tizen. Returns the route before and after, so the log says whether
         * the page actually moved.
         */
        val BACK_KEY_JS = """
            (function () {
              var before = location.hash;
              [27, 461, 10009].forEach(function (code) {
                ['keydown', 'keyup'].forEach(function (type) {
                  var e = new KeyboardEvent(type, {
                    bubbles: true, cancelable: true, key: 'Escape', code: 'Escape'
                  });
                  Object.defineProperty(e, 'keyCode', { get: function () { return code; } });
                  Object.defineProperty(e, 'which', { get: function () { return code; } });
                  document.dispatchEvent(e);
                });
              });
              return before + ' -> ' + location.hash;
            })();
        """.trimIndent()

        /**
         * Arrow-key presses dispatched to the focused element, so they reach
         * the page's own key handlers the way a remote's d-pad would.
         */
        fun arrowKeysJs(keys: List<ArrowKey>): String {
            val list = keys.joinToString(",") { "['${it.key}',${it.keyCode}]" }
            return "(function(){var t=document.activeElement||document.body;" +
                "[$list].forEach(function(k){['keydown','keyup'].forEach(function(type){" +
                "var e=new KeyboardEvent(type,{bubbles:true,cancelable:true,key:k[0],code:k[0]});" +
                "Object.defineProperty(e,'keyCode',{get:function(){return k[1];}});" +
                "Object.defineProperty(e,'which',{get:function(){return k[1];}});" +
                "t.dispatchEvent(e);});});})();"
        }

        /** The open pill's width, its right margin, and the folded tab's size, in dp. */
        const val PILL_WIDTH_DP = 76
        const val PILL_MARGIN_END_DP = 6
        const val HANDLE_WIDTH_DP = 60
        const val HANDLE_HEIGHT_DP = 32

        /** How long Android Auto keeps its strip on screen after a touch. */
        const val CONTROLS_VISIBLE_MS = 10_000L

        /** How long after the last scroll event the finger lifts. */
        const val GESTURE_IDLE_MS = 140L
    }
}
