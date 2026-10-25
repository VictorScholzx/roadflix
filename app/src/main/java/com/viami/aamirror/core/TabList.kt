package com.viami.aamirror.core

/** One open browser tab: what it shows and what its chip says. */
data class TabInfo(val url: String, val title: String = "")

/**
 * The car browser's open tabs and which one is showing. Pure state: the
 * browser keeps one WebView per entry, in the same order.
 */
data class TabList(val tabs: List<TabInfo>, val active: Int) {

    init {
        require(tabs.isNotEmpty() && active in tabs.indices)
    }

    val size: Int get() = tabs.size

    val current: TabInfo get() = tabs[active]

    val canOpen: Boolean get() = size < MAX_TABS

    /** A new tab at the end, shown at once; unchanged when the limit is reached. */
    fun open(url: String): TabList =
        if (!canOpen) this else TabList(tabs + TabInfo(url), tabs.size)

    fun select(index: Int): TabList =
        if (index in tabs.indices) copy(active = index) else this

    /**
     * Closes a tab. The showing tab moves to its left neighbour when it is the one
     * closed; closing the only tab leaves a fresh one on [homeUrl] instead of none.
     */
    fun close(index: Int, homeUrl: String): TabList {
        if (index !in tabs.indices) return this
        if (size == 1) return single(homeUrl)
        val remaining = tabs.filterIndexed { i, _ -> i != index }
        val newActive = when {
            index < active -> active - 1
            index == active -> (active - 1).coerceAtLeast(0)
            else -> active
        }
        return TabList(remaining, newActive)
    }

    fun update(index: Int, url: String, title: String): TabList =
        if (index !in tabs.indices) this
        else copy(tabs = tabs.mapIndexed { i, tab -> if (i == index) TabInfo(url, title) else tab })

    /** What a tab's chip shows: "Início" on the start page, else the page title or its site. */
    fun label(index: Int): String {
        val tab = tabs[index]
        return when {
            HomePage.isHome(tab.url) -> "Início"
            tab.title.isNotBlank() -> tab.title.trim()
            else -> HomePage.hostOf(tab.url)
        }
    }

    companion object {
        /** Each tab keeps a live page in memory; a few is plenty on a car screen. */
        const val MAX_TABS = 5

        fun single(url: String) = TabList(listOf(TabInfo(url)), 0)
    }
}
