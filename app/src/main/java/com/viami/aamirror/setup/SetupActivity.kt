package com.viami.aamirror.setup

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.viami.aamirror.R
import com.viami.aamirror.browser.BrowserDisplay
import com.viami.aamirror.core.AppTheme
import com.viami.aamirror.core.UrlResolver
import com.viami.aamirror.core.MirrorEvent
import com.viami.aamirror.core.MirrorGateway
import com.viami.aamirror.core.MirrorSettings
import com.viami.aamirror.core.MirrorState
import com.viami.aamirror.core.Palette
import com.viami.aamirror.core.ProjectionStatus
import com.viami.aamirror.input.BrightnessSaver
import com.viami.aamirror.input.MirrorAccessibilityService
import com.viami.aamirror.input.RotationLock
import com.viami.aamirror.mirror.MirrorService
import kotlinx.coroutines.launch

class SetupActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val capture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                MirrorService.start(this, result.resultCode, data)
            } else {
                MirrorGateway.dispatch(MirrorEvent.PermissionDenied)
            }
        }

    /** The palette currently painted; render() colours the status values from it. */
    private var palette: Palette = AppTheme.DEFAULT.palette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        SettingsStore.load(this)

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        findViewById<View>(R.id.row_touch).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<View>(R.id.row_rotation).setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"),
                )
            )
        }
        findViewById<View>(R.id.row_dim).setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:$packageName"),
                )
            )
        }
        findViewById<View>(R.id.row_capture).setOnClickListener { requestCapture() }
        findViewById<Button>(R.id.btn_open_url).setOnClickListener { openUrlInCar() }
        findViewById<View>(R.id.row_bookmarks).setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }
        findViewById<View>(R.id.row_android_auto).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.row_android_auto)
                .setMessage(R.string.setup_instructions)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
        findViewById<EditText>(R.id.edit_url).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                openUrlInCar(); true
            } else {
                false
            }
        }
        findViewById<Switch>(R.id.switch_auto).apply {
            isChecked = MirrorSettings.autoTheme
            setOnCheckedChangeListener { _, checked ->
                MirrorSettings.autoTheme = checked
                SettingsStore.save(this@SetupActivity)
                applyTheme()
            }
        }
        findViewById<View>(R.id.row_auto).setOnClickListener {
            findViewById<Switch>(R.id.switch_auto).toggle()
        }

        buildSwatches()
        applyTheme()

        lifecycleScope.launch {
            MirrorGateway.state.collect { render(it) }
        }

        if (intent.getBooleanExtra(EXTRA_REQUEST_CAPTURE, false)) requestCapture()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_REQUEST_CAPTURE, false)) requestCapture()
    }

    override fun onResume() {
        super.onResume()
        render(MirrorGateway.state.value)
    }

    private fun openUrlInCar() {
        val query = findViewById<EditText>(R.id.edit_url).text.toString()
        if (query.isBlank()) return
        if (BrowserDisplay.isAttached) {
            BrowserDisplay.loadUrl(UrlResolver.resolve(query))
            Toast.makeText(this, R.string.opened_in_car, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.car_not_connected, Toast.LENGTH_LONG).show()
        }
    }

    private fun requestCapture() {
        if (MirrorGateway.state.value.projection == ProjectionStatus.ACTIVE) return
        val manager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        capture.launch(manager.createScreenCaptureIntent())
    }

    private fun render(state: MirrorState) {
        findViewById<TextView>(R.id.txt_status).text = when {
            state.isMirroring -> getString(R.string.status_mirroring)
            state.projection == ProjectionStatus.ACTIVE -> getString(R.string.status_ready)
            state.projection == ProjectionStatus.REQUESTED -> getString(R.string.status_waiting)
            state.lastError != null -> state.lastError
            else -> getString(R.string.status_idle)
        }
        setValue(R.id.txt_touch, MirrorAccessibilityService.instance != null)
        setValue(R.id.txt_rotation, RotationLock.canLock(this))
        setValue(R.id.txt_dim, BrightnessSaver.canDim(this))
        findViewById<TextView>(R.id.txt_capture).apply {
            val active = state.projection == ProjectionStatus.ACTIVE
            text = getString(if (active) R.string.perm_on else R.string.perm_capture_action)
            setTextColor(if (active) palette.muted else palette.accent)
        }
        findViewById<TextView>(R.id.txt_bookmarks).text =
            "${BookmarkStore.load(this).size}  ›"
    }

    /** "Ativo" in the quiet colour when granted, "Ativar" in the accent when it still needs a tap. */
    private fun setValue(id: Int, granted: Boolean) {
        findViewById<TextView>(id).apply {
            text = getString(if (granted) R.string.perm_on else R.string.perm_off)
            setTextColor(if (granted) palette.muted else palette.accent)
        }
    }

    private fun isNight(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    private fun buildSwatches() {
        val row = findViewById<LinearLayout>(R.id.row_swatches)
        AppTheme.entries.forEach { theme ->
            val dot = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(dp(38), dp(38), Gravity.CENTER)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(theme.palette.ground)
                    setStroke(dp(7), theme.palette.accent)
                }
            }
            val cell = FrameLayout(this).apply {
                tag = theme
                layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f)
                contentDescription = theme.label
                isClickable = true
                isFocusable = true
                addView(FrameLayout(this@SetupActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(dp(50), dp(50), Gravity.CENTER)
                    addView(dot)
                })
                setOnClickListener {
                    MirrorSettings.theme = theme
                    SettingsStore.save(this@SetupActivity)
                    applyTheme()
                }
            }
            row.addView(cell)
        }
    }

    /** Paints the whole screen from the chosen theme (or the automatic day/night pick). */
    private fun applyTheme() {
        val shown = AppTheme.resolve(MirrorSettings.theme, MirrorSettings.autoTheme, isNight())
        palette = shown.palette
        val p = palette

        window.decorView.setBackgroundColor(p.ground)
        if (Build.VERSION.SDK_INT < 35) {
            // Before Android 15 the system bars are painted by the window, not drawn behind it.
            @Suppress("DEPRECATION")
            window.statusBarColor = p.ground
            @Suppress("DEPRECATION")
            window.navigationBarColor = p.ground
        }
        findViewById<View>(R.id.root).setBackgroundColor(p.ground)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = p.isLight
            isAppearanceLightNavigationBars = p.isLight
        }

        findViewById<TextView>(R.id.txt_title).setTextColor(p.text)
        findViewById<TextView>(R.id.txt_status).setTextColor(p.muted)
        findViewById<View>(R.id.dot_status).background = oval(p.accent)

        for (id in intArrayOf(R.id.card_theme, R.id.card_permissions, R.id.card_send, R.id.card_links)) {
            val card = findViewById<ViewGroup>(id)
            card.background = rounded(p.surface, 20)
            card.clipToOutline = true
            paintRoles(card, p)
        }
        paintRoles(findViewById(R.id.root), p)

        findViewById<TextView>(R.id.txt_theme_name).apply {
            setTextColor(p.text)
            text = if (MirrorSettings.autoTheme) {
                "${MirrorSettings.theme.label} · ${getString(R.string.theme_auto).lowercase()}"
            } else {
                MirrorSettings.theme.label
            }
        }
        findViewById<View>(R.id.rule_theme).setBackgroundColor(p.raised)
        findViewById<Switch>(R.id.switch_auto).apply {
            thumbTintList = ColorStateList.valueOf(0xFFFFFFFF.toInt())
            trackTintList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(p.switchOn, p.switchOff),
            )
        }
        val row = findViewById<LinearLayout>(R.id.row_swatches)
        for (i in 0 until row.childCount) {
            val cell = row.getChildAt(i) as ViewGroup
            val selected = cell.tag == MirrorSettings.theme
            cell.isSelected = selected
            cell.getChildAt(0).background = if (selected) ring(p.text) else null
        }

        findViewById<TextView>(R.id.lbl_url).setTextColor(p.text)
        findViewById<EditText>(R.id.edit_url).apply {
            background = rounded(p.raised, 12)
            setTextColor(p.text)
            setHintTextColor(p.muted)
        }
        findViewById<Button>(R.id.btn_open_url).apply {
            background = rounded(p.button, 25)
            setTextColor(p.buttonText)
        }
        findViewById<TextView>(R.id.txt_android_auto).setTextColor(p.muted)
        findViewById<TextView>(R.id.txt_bookmarks).setTextColor(p.muted)

        // The car start page follows the same theme.
        BrowserDisplay.refreshHome()
        render(MirrorGateway.state.value)
    }

    /** Colours every view by the role tag its style gives it. */
    private fun paintRoles(view: View, p: Palette) {
        when (view.tag) {
            "section", "sub" -> (view as TextView).setTextColor(p.muted)
            "title" -> (view as TextView).setTextColor(p.text)
            "rule" -> view.setBackgroundColor(p.raised)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) paintRoles(view.getChildAt(i), p)
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics,
        ).toInt()

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun oval(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun ring(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0)
        setStroke(dp(2), color)
    }

    companion object {
        const val EXTRA_REQUEST_CAPTURE = "request_capture"
    }
}
