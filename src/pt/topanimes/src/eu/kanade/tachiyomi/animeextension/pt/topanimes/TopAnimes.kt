package eu.kanade.tachiyomi.animeextension.pt.topanimes

import eu.kanade.tachiyomi.animeextension.pt.topanimes.extractors.TopAnimesExtractor
import eu.kanade.tachiyomi.animesource.model.Anime
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.ParsedAnimeHttpSource
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.util.asJsoup
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.injectLazy

class TopAnimes : ParsedAnimeHttpSource() {

    override val name = "TopAnimes"
    override val baseUrl = "https://topanimes.net"
    override val lang = "pt-BR"
    override val supportsLatest = true

    private val json: Json by injectLazy()

    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        val videos = mutableListOf<Video>()

        // Procura o link encriptado no HTML
        val playerLink = document.select("a[href*='auth=']").attr("href")

        if (playerLink.isNotEmpty()) {
            val extractor = TopAnimesExtractor(json)
            val extractedVideos = extractor.videosFromUrl(playerLink)

            videos.addAll(extractedVideos)
        }

        return videos
    }

    override fun popularAnimeRequest(page: Int): Request = GET("$baseUrl/animes/page/$page")
    override fun popularAnimeSelector(): String = "div.poster"
    override fun popularAnimeFromElement(element: Element): Anime {
        val anime = Anime.create()
        anime.setUrlWithoutDomain(element.select("a").attr("href"))
        anime.title = element.select("img").attr("alt")
        anime.thumbnail_url = element.select("img").attr("src")
        return anime
    }

    override fun animeDetailsParse(document: Document): Anime {
        val anime = Anime.create()
        anime.title = document.select("h1.title").text()
        anime.genre = document.select("div.genres a").joinToString { it.text() }
        anime.description = document.select("div.sinopse, div.desc").text()
        return anime
    }

    override fun episodeListSelector(): String = "ul.episodios li"
    override fun episodeFromElement(element: Element): SEpisode {
        val episode = SEpisode.create()
        val link = element.select("div.episodiotitle > a").attr("href")
        episode.setUrlWithoutDomain(link)
        episode.name = element.select("div.episodiotitle > a").text()
        val numRaw = element.select("div.epnumber").text()
        episode.episode_number = numRaw.toFloatOrNull() ?: 0f
        return episode
    }

    // Boilerplate padrão
    override fun latestUpdatesRequest(page: Int) = popularAnimeRequest(page)
    override fun latestUpdatesSelector() = popularAnimeSelector()
    override fun latestUpdatesFromElement(element: Element) = popularAnimeFromElement(element)
    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList) = GET("$baseUrl/?s=$query")
    override fun searchAnimeSelector() = popularAnimeSelector()
    override fun searchAnimeFromElement(element: Element) = popularAnimeFromElement(element)
}
