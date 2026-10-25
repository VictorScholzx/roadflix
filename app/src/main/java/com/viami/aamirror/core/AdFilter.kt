package com.viami.aamirror.core

/**
 * Recognises the ad-network redirects that sites full of ads throw at the browser
 * ("Atenção!" pages on throwaway domains such as https://x.space/click?key=…), so they are
 * neither opened nor kept in the history.
 */
object AdFilter {

    /** Pop-under and redirect networks, matched with their subdomains. */
    private val AD_HOSTS = listOf(
        "adcash.com", "popads.net", "popcash.net", "propellerads.com", "exoclick.com",
        "adsterra.com", "onclickads.net", "hilltopads.net", "clickadu.com", "admaven.com",
    )

    /** Query keys the redirect scripts put on their "/click" URLs. */
    private val CLICK_KEYS = listOf("key=", "zone=", "campaign=", "cid=")

    fun isAdRedirect(url: String?): Boolean {
        if (url == null) return false
        val host = HomePage.hostOf(url)
        if (AD_HOSTS.any { host == it || host.endsWith(".$it") }) return true
        val path = url.substringAfter("://").substringAfter('/', "").substringBefore('?').trimEnd('/')
        val query = url.substringAfter('?', "")
        return path == "click" && CLICK_KEYS.any { query.contains(it) }
    }
}
