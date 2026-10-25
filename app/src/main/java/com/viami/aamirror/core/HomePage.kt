package com.viami.aamirror.core

/** One favourite on the browser start page; [icon] is a data: URI, or null to show the initial. */
data class HomeTile(val title: String, val url: String, val icon: String?)

/** A row of "Recentes"; [label] is when it was opened ("14:10", "Ontem"), [icon] as in [HomeTile]. */
data class RecentLink(val title: String, val url: String, val label: String, val icon: String? = null)

/** The link last sent from the phone; [age] reads like "há 2 min". */
data class SentLink(val title: String, val url: String, val age: String)

/**
 * The browser start page shown on the car display: clock, a search pill that
 * opens the car keyboard, and the favourites as large tiles. It is built
 * locally, so it works with no connection; only the sites behind it need one.
 */
object HomePage {

    /** Base URL the page is loaded under; also how the browser recognises it. */
    const val URL = "https://roadflix.home/"

    /** Name of the JavaScript bridge the search pill calls. */
    const val BRIDGE = "RoadFlix"

    fun isHome(url: String?): Boolean = url != null && url.startsWith(URL)

    /** "www.g1.globo.com/x" → "g1.globo.com"; what the favicon cache is keyed by. */
    fun hostOf(url: String): String =
        url.trim()
            .substringAfter("://")
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore(':')
            .removePrefix("www.")
            .lowercase()

    /** The letter shown on a tile with no icon. */
    fun initialOf(title: String): String =
        title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    fun render(
        palette: Palette,
        tiles: List<HomeTile>,
        recents: List<RecentLink> = emptyList(),
        sent: SentLink? = null,
    ): String {
        val p = palette
        val sentBlock = sent?.let {
            "<div class=\"item\"><a class=\"sent\" href=\"${escape(it.url)}\">" +
                "<svg width=\"22\" height=\"22\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"${hex(p.accent)}\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><rect x=\"7\" y=\"2\" width=\"10\" height=\"20\" rx=\"2\"/><path d=\"M11 18h2\"/></svg>" +
                "<span class=\"txt\"><span class=\"t\">${escape(it.title)}</span>" +
                "<span class=\"s\">Enviado do celular ${escape(it.age)}</span></span>" +
                "<span class=\"open\">Abrir</span></a>" +
                "<button class=\"del\" aria-label=\"Remover\" onclick=\"$BRIDGE.removeSent()\"><svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3\"/></svg></button></div>"
        } ?: ""
        val rows = recents.joinToString("") {
            val mark = if (it.icon != null) {
                "<span class=\"mini\"><img src=\"${escape(it.icon)}\" alt=\"\"></span>"
            } else {
                "<span class=\"mini letter\">${escape(initialOf(it.title))}</span>"
            }
            "<div class=\"item\"><a class=\"row\" href=\"${escape(it.url)}\">$mark<span class=\"t\">${escape(it.title)}</span>" +
                "<span class=\"w\">${escape(it.label)}</span></a>" +
                "<button class=\"del\" aria-label=\"Remover\" data-url=\"${escape(it.url)}\" " +
                "onclick=\"$BRIDGE.removeRecent(this.dataset.url)\"><svg width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3\"/></svg></button></div>"
        }
        val recentBody = when {
            rows.isNotEmpty() -> rows
            sentBlock.isEmpty() -> "<p class=\"empty\">Os sites que você abrir aparecem aqui.</p>"
            else -> ""
        }
        val cells = tiles.joinToString("") { tile ->
            val mark = if (tile.icon != null) {
                "<span class=\"icon\"><img src=\"${escape(tile.icon)}\" alt=\"\"></span>"
            } else {
                "<span class=\"icon letter\">${escape(initialOf(tile.title))}</span>"
            }
            "<a class=\"tile\" href=\"${escape(tile.url)}\">$mark<span class=\"name\">${escape(tile.title)}</span></a>"
        }
        return """
            <!doctype html>
            <html lang="pt-BR"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Início</title>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            html,body{height:100%;background:${hex(p.ground)};color:${hex(p.text)};
              font-family:'Inter',system-ui,-apple-system,Roboto,sans-serif;
              -webkit-font-smoothing:antialiased;overflow-x:hidden}
            body{padding:3.5vh 104px 3vh 3vw;display:flex;flex-direction:column;gap:3vh}
            header{display:flex;align-items:center;gap:3vw}
            .clock{font-size:min(9vh,40px);font-weight:600;letter-spacing:-.02em;line-height:1;
              font-variant-numeric:tabular-nums}
            .date{font-size:min(3.4vh,16px);color:${hex(p.muted)};margin-top:.6vh}
            .search{flex:1;height:min(11vh,60px);border:0;border-radius:999px;background:${hex(p.surface)};
              color:${hex(p.muted)};font-size:min(4vh,19px);font-weight:500;font-family:inherit;text-align:left;
              display:flex;align-items:center;gap:14px;padding:0 24px}
            .search svg{flex:none}
            .card{flex:1;min-height:0;background:${hex(p.surface)};border-radius:20px;padding:2.6vh 22px;
              display:flex;flex-direction:column;gap:2vh}
            h2{font-size:min(3.8vh,18px);font-weight:600}
            .grid{flex:1;display:grid;grid-template-columns:repeat(4,minmax(0,1fr));
              grid-auto-rows:min(26vh,150px);align-content:start;gap:14px;overflow-y:auto}
            .tile{background:${hex(p.raised)};border-radius:16px;color:${hex(p.text)};text-decoration:none;
              display:flex;flex-direction:column;align-items:center;justify-content:center;gap:1.4vh;
              font-size:min(3.6vh,16px);font-weight:500;padding:8px;min-height:0}
            .tile:active{opacity:.6}
            .icon{width:min(12vh,60px);height:min(12vh,60px);border-radius:24%;background:#fff;
              display:flex;align-items:center;justify-content:center;flex:none}
            .icon img{width:66%;height:66%;object-fit:contain}
            .icon.letter{background:${hex(p.ground)};color:${hex(p.text)};font-size:min(6vh,26px);font-weight:500}
            .name{max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
            .empty{color:${hex(p.muted)};font-size:min(3.6vh,16px)}
            .cols{flex:1;min-height:0;display:flex;gap:14px}
            .cols .card{min-width:0}
            .recent{flex:0 0 34%;overflow:hidden}
            .list{display:flex;flex-direction:column;overflow-y:auto}
            .sent{display:flex;align-items:center;gap:12px;background:${hex(p.raised)};border-radius:14px;
              padding:1.6vh 14px;color:${hex(p.text)};text-decoration:none;margin-bottom:.8vh}
            .sent .txt{flex:1;min-width:0;display:flex;flex-direction:column;gap:3px}
            .t{font-size:min(3.6vh,16px);font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
            .s{font-size:min(3vh,13px);color:${hex(p.muted)}}
            .open{font-size:min(3.4vh,15px);font-weight:600;flex:none}
            .row{display:flex;align-items:center;justify-content:space-between;gap:12px;min-height:min(12vh,62px);
              border-top:1px solid ${hex(p.raised)};color:${hex(p.text)};text-decoration:none;padding:0 2px}
            .item{display:flex;align-items:center;gap:6px}
            .item .row,.item .sent{flex:1;min-width:0}
            .list>.item:first-child .row{border-top:0}
            .del{flex:none;width:min(10vh,44px);height:min(10vh,44px);border:0;border-radius:12px;background:transparent;
              color:${hex(p.muted)};display:flex;align-items:center;justify-content:center}
            .del:active{background:${hex(p.raised)}}
            .w{flex:none;font-size:min(3vh,13px);color:${hex(p.muted)}}
            .row .t{flex:1;min-width:0}
            .mini{width:min(5.2vh,24px);height:min(5.2vh,24px);border-radius:24%;background:#fff;flex:none;
              display:flex;align-items:center;justify-content:center}
            .mini img{width:70%;height:70%;object-fit:contain}
            .mini.letter{background:${hex(p.ground)};color:${hex(p.text)};font-size:min(2.8vh,12px);font-weight:500}
            </style></head>
            <body>
            <header>
              <div><div class="clock" id="clock">--:--</div><div class="date" id="date"></div></div>
              <button class="search" onclick="$BRIDGE.search()">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="${hex(p.text)}" stroke-width="1.8" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/></svg>
                Pesquisar ou digitar endereço
              </button>
            </header>
            <div class="cols">
            <section class="card">
              <h2>Favoritos</h2>
              ${if (cells.isEmpty()) "<p class=\"empty\">Adicione favoritos no celular, em RoadFlix → Favoritos.</p>" else "<div class=\"grid\">$cells</div>"}
            </section>
            <section class="card recent">
              <h2>Recentes</h2>
              <div class="list">$sentBlock$recentBody</div>
            </section>
            </div>
            <script>
            function tick(){var d=new Date();
              document.getElementById('clock').textContent=d.toLocaleTimeString('pt-BR',{hour:'2-digit',minute:'2-digit'});
              var s=d.toLocaleDateString('pt-BR',{weekday:'long',day:'numeric',month:'long'});
              document.getElementById('date').textContent=s.charAt(0).toUpperCase()+s.slice(1);}
            tick();setInterval(tick,10000);
            </script>
            </body></html>
        """.trimIndent()
    }

    fun hex(color: Int): String = "#%06X".format(color and 0xFFFFFF)

    private fun escape(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
