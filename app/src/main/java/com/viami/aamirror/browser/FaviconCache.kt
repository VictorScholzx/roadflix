package com.viami.aamirror.browser

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Site icons for the start page, fetched once per host and kept in the app's
 * files so the favourites keep their icons with no connection.
 */
object FaviconCache {

    private const val TAG = "FaviconCache"
    private const val DIR = "favicons"

    /** The icon a site declares for home screens, then Google's favicon service as a fallback. */
    private fun sources(host: String) = listOf(
        "https://$host/apple-touch-icon.png",
        "https://www.$host/apple-touch-icon.png",
        "https://www.google.com/s2/favicons?domain=$host&sz=128",
    )

    private fun file(context: Context, host: String) =
        File(File(context.filesDir, DIR), "$host.png")

    /** The cached icon as a data: URI, or null when it has not been fetched (or does not exist). */
    fun dataUri(context: Context, host: String): String? {
        val f = file(context, host)
        if (!f.isFile || f.length() == 0L) return null
        return "data:image/png;base64," + Base64.encodeToString(f.readBytes(), Base64.NO_WRAP)
    }

    /**
     * Downloads the icons not cached yet. Returns true when at least one new icon
     * arrived, so the caller knows the start page is worth redrawing.
     */
    suspend fun fetchMissing(context: Context, hosts: Collection<String>): Boolean =
        withContext(Dispatchers.IO) {
            var fetched = false
            for (host in hosts.distinct()) {
                if (host.isBlank() || file(context, host).isFile) continue
                val bytes = sources(host).firstNotNullOfOrNull { download(it) } ?: continue
                file(context, host).apply { parentFile?.mkdirs() }.writeBytes(bytes)
                fetched = true
            }
            fetched
        }

    /** The image bytes, or null for a failed request, a non-image or a tiny placeholder icon. */
    private fun download(url: String): ByteArray? = try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            instanceFollowRedirects = true
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                val bytes = connection.inputStream.use { it.readBytes() }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                // Anything under 32 px is a generic globe or a broken image, not worth a tile.
                if (bounds.outWidth >= 32) bytes else null
            }
        } finally {
            connection.disconnect()
        }
    } catch (e: Exception) {
        Log.i(TAG, "no icon at $url: ${e.message}")
        null
    }
}
