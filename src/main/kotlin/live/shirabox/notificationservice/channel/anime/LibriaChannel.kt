package live.shirabox.notificationservice.channel.anime

import fuel.httpGet
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import live.shirabox.notificationservice.AppNotification
import live.shirabox.notificationservice.Util
import live.shirabox.notificationservice.channel.Channel
import java.net.URI

class LibriaChannel : Channel("Libria") {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }
    private val _apiHost = "api.anilibria.tv"

    override suspend fun listen() {
        val client = LibriaSocketClient(
            uri = URI("ws://$_apiHost/v3/ws/"),
            reconnect = true,
            onConnect = {
                // Subscribe only on encoding finish messages
                it.send(json.encodeToString(
                    Subscribe(
                        subscribe = SubscriptionType("encode_finish")
                    )
                ))
            },
            onReceive = {
                it?.let {
                    try {
                        val subscriptionResponse = json.decodeFromString<SubscriptionResponse>(it)

                        logger.info("Subscription response: ${subscriptionResponse.subscribe}")
                    } catch (_: Exception) { /* Ignore other types of messages */ }

                    try {
                        val encodeFinishData = json.decodeFromString<RootWrapper<EncodeFinishData>>(it)
                        val names = runBlocking { namesFromId(encodeFinishData.data.id) }

                        names?.let {
                            this@LibriaChannel.notifyObservers(  // Fire registered observers
                                AppNotification(
                                    topic = "anilibria_${names.first}",
                                    title = names.second,
                                    text = "Вышел ${encodeFinishData.data.episode} эпизод в озвучке AniLibria!"
                                )
                            )

                            logger.info("Released #${encodeFinishData.data.episode} episode for title with " +
                                    "id ${encodeFinishData.data.id}")
                        }
                    } catch (ex: Exception) { /* Ignore other types of messages */ }
                }
            }
        )
        client.connect()
    }

    private suspend fun namesFromId(id: Int): Pair<String, String>? {
        val response = "https://$_apiHost/v3/title".httpGet(
            listOf("id" to "$id")
        ).also {
            if (it.statusCode != 200) {
                logger.error("(${it.statusCode}): Failed to fetch title data with id $id")
                return null
            }
        }

        val data = json.decodeFromString<LibriaAnimeData>(response.body)

        return Util.encodeString(data.names.en) to data.names.ru
    }
}