package live.shirabox.notificationservice.channel.anime

import fuel.httpGet
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import live.shirabox.notificationservice.AppNotification
import live.shirabox.notificationservice.channel.Channel
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class LibriaChannel : Channel("Libria") {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }
    private val _apiHost = "api.anilibria.tv"

    private val client = HttpClient(OkHttp) {
        engine {
            preconfigured = OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
            pingInterval = 30_000
        }
    }

    override suspend fun listen() {
        coroutineScope {
            launch(Dispatchers.IO) {
                logger.info("Started listening WebSocket")

                client.webSocket(method = HttpMethod.Get, host = _apiHost, port = 80, path = "/v3/ws/") {

                    // Subscribe only on encoding finish messages
                    sendSerialized(
                        Subscribe(
                            subscribe = SubscriptionType("encode_end")
                        )
                    )

                    while (true) {
                        try {
                            val socketInitMessage = receiveDeserialized<SocketInitMessage>()

                            val connection = socketInitMessage.connection.replaceFirstChar { it.uppercase() }
                            val apiVersion = socketInitMessage.apiVersion

                            logger.info("$connection connection, API version: $apiVersion")
                        } catch (_: Exception) { /* Ignore other types of messages */ }

                        try {
                            val subscriptionResponse = receiveDeserialized<SubscriptionResponse>()

                            logger.info("Subscription response: ${subscriptionResponse.subscribe}")
                        } catch (_: Exception) { /* Ignore other types of messages */ }

                        try {
                            val encodeFinishData = receiveDeserialized<RootWrapper<EncodeFinishData>>()
                            val names = namesFromId(encodeFinishData.data.id)

                            names?.let {
                                this@LibriaChannel.notifyObservers(  // Fire registered observers
                                    AppNotification(
                                        topic = "anilibria_${names.first}",
                                        title = names.second,
                                        text = "Вышел ${encodeFinishData.data.episode} эпизод в озвучке AniLibria!"
                                    )
                                )

                                logger.info("Released #${encodeFinishData.data.episode} episode for title with id ${encodeFinishData.data.id}")
                            }
                        } catch (ex: Exception) { /* Ignore other types of messages */ }
                    }
                }
                client.close()
            }
        }
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

        return data.code to data.names.ru
    }

}