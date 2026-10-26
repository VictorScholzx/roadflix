package com.viami.aamirror.car

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.car.app.model.CarIcon
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * Car icons sent to the host as bitmaps rather than resource references.
 * Android Auto caches resource icons by their numeric ID, and those IDs move
 * whenever a drawable is added or removed, so a reference could come back
 * showing an old, unrelated picture.
 */
object CarIcons {

    private const val SIZE_PX = 96

    fun of(context: Context, iconRes: Int): CarIcon {
        val drawable = requireNotNull(ContextCompat.getDrawable(context, iconRes))
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, SIZE_PX, SIZE_PX)
        drawable.draw(Canvas(bitmap))
        return CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
    }
}
