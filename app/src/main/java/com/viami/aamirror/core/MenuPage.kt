package com.viami.aamirror.core

/**
 * RoadFlix's own main menu on the car display: three large cards drawn by the
 * app rather than Android Auto's grid, so it can follow the approved design and
 * the chosen theme. Each card is a "roadflix://" link the browser hands back to
 * the car screen instead of loading.
 */
object MenuPage {

    /** Base URL the menu is loaded under. */
    const val URL = "https://roadflix.menu/"

    const val SCHEME = "roadflix"

    /** The app-link target ("mirror", "browser", "youtube") of a roadflix:// URL, or null. */
    fun target(url: String?): String? {
        if (url == null || !url.startsWith("$SCHEME://")) return null
        return url.removePrefix("$SCHEME://").trimEnd('/').ifEmpty { null }
    }

    private data class Card(val target: String, val title: String, val subtitle: String, val icon: String)

    private val cards = listOf(
        Card(
            "mirror", "Espelhar tela", "A tela do celular na central",
            "<rect x=\"7\" y=\"2\" width=\"10\" height=\"20\" rx=\"2\"/><path d=\"M11 18h2\"/>",
        ),
        Card(
            "browser", "Navegador", "Favoritos, recentes e abas",
            "<circle cx=\"12\" cy=\"12\" r=\"9\"/><path d=\"M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18\"/>",
        ),
        Card(
            "youtube", "YouTube TV", "Vídeos com o som do carro",
            "<rect x=\"2\" y=\"5\" width=\"20\" height=\"14\" rx=\"3\"/><path d=\"M10 9l5 3-5 3z\"/>",
        ),
    )

    fun render(palette: Palette): String {
        val p = palette
        val hex = HomePage::hex
        val items = cards.joinToString("") { card ->
            "<a class=\"card\" href=\"$SCHEME://${card.target}\">" +
                "<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"${hex(p.text)}\" stroke-width=\"1.2\" " +
                "stroke-linecap=\"round\" stroke-linejoin=\"round\">${card.icon}</svg>" +
                "<span class=\"title\">${card.title}</span><span class=\"sub\">${card.subtitle}</span></a>"
        }
        return """
            <!doctype html>
            <html lang="pt-BR"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>RoadFlix</title>
            <style>
            *{box-sizing:border-box;margin:0;padding:0}
            html,body{height:100%;background:${hex(p.ground)};color:${hex(p.text)};
              font-family:'Inter',system-ui,-apple-system,Roboto,sans-serif;-webkit-font-smoothing:antialiased;overflow:hidden}
            body{padding:4.5vh 3.2vw 5vh 3.2vw;display:flex;flex-direction:column;gap:4.5vh}
            h1{padding-right:104px;font-size:min(6.5vh,30px);font-weight:600;letter-spacing:-.02em;line-height:1.1}
            .cards{flex:1;min-height:0;display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px}
            .card{background:${hex(p.surface)};border-radius:20px;color:${hex(p.text)};text-decoration:none;
              display:flex;flex-direction:column;align-items:center;justify-content:center;gap:2.4vh;
              padding:3vh 18px;text-align:center}
            .card:active{opacity:.6}
            .card svg{width:min(19vh,112px);height:min(19vh,112px)}
            .title{font-size:min(5.6vh,28px);font-weight:600;letter-spacing:-.01em}
            .sub{font-size:min(3.6vh,17px);color:${hex(p.muted)};margin-top:-1.2vh}
            </style></head>
            <body>
            <h1>RoadFlix</h1>
            <nav class="cards">$items</nav>
            </body></html>
        """.trimIndent()
    }
}
