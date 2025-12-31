package eu.kanade.tachiyomi.animeextension.pt.topanimes.extractors

import android.util.Base64
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLDecoder

class TopAnimesExtractor(private val json: Json) {

    fun videosFromUrl(url: String): List<Video> {
        val videoList = mutableListOf<Video>()

        try {
            // 1. Limpa a URL para pegar só o hash
            val authRaw = url.substringAfter("auth=")
            val authClean = URLDecoder.decode(authRaw, "UTF-8")

            // 2. Decodifica o Base64
            val jsonString = String(Base64.decode(authClean, Base64.DEFAULT))

            // 3. Lê o JSON
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            val videoUrl = jsonObject["url"]?.jsonPrimitive?.content ?: return emptyList()

            // 4. Cria o objeto de vídeo
            if (videoUrl.contains(".m3u8")) {
                videoList.add(Video(videoUrl, "Legendado/Dublado (HLS)", videoUrl))
            } else {
                videoList.add(Video(videoUrl, "Default", videoUrl))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return videoList
    }
}
