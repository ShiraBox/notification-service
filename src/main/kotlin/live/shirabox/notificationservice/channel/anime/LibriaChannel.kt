package live.shirabox.notificationservice.channel.anime

import fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
        coroutineScope {
            val client = LibriaSocketClient(
                uri = URI("ws://$_apiHost/v3/ws/"),
                onConnect = {
                    // Subscribe only on encoding finish messages
                    it.send(json.encodeToString(
                        Subscribe(
                            subscribe = SubscriptionType("encode_finish")
                        )
                    ))
                },
                onDisconnect = {
                    launch(Dispatchers.IO) { it.reconnect() }
                },
                onReceive = { response ->
                    response?.let { str ->
                        try {
                            val subscriptionResponse = json.decodeFromString<SubscriptionResponse>(str)

                            logger.info("Subscription response: ${subscriptionResponse.subscribe}")
                        } catch (_: Exception) { /* Ignore other types of messages */ }

                        try {
                            val encodeFinishData = json.decodeFromString<RootWrapper<EncodeFinishData>>(str)
                            val names = runBlocking {
                                namesFromId(encodeFinishData.data.id).catch {
                                    it.printStackTrace()
                                    emitAll(emptyFlow())
                                }.firstOrNull()
                            }

                            names?.let {
                                this@LibriaChannel.notifyObservers(  // Fire registered observers
                                    AppNotification(
                                        topic = "anilibria_${names.first}",
                                        title = names.second,
                                        text = "Вышел ${encodeFinishData.data.episode} эпизод в озвучке AniLibria!",
                                        data = mapOf(
                                            "code" to names.first
                                        )
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
    }

    private suspend fun namesFromId(id: Int): Flow<Pair<String, String>> = flow {
        try {
            val response = "https://$_apiHost/v3/title".httpGet(
                listOf("id" to "$id")
            ).also {
                if (it.statusCode != 200) {
                    logger.error("(${it.statusCode}): Failed to fetch title data with id $id")
                    emitAll(emptyFlow())
                }
            }

            val data = json.decodeFromString<LibriaAnimeData>(response.body)

            emit(Util.encodeString(data.names.en) to data.names.ru)
        } catch (ex: Exception) {
            throw ex
        }
    }
}