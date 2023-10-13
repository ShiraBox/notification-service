package live.shirabox.notificationservice.channel.update

import fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.serialization.json.Json
import live.shirabox.notificationservice.AppNotification
import live.shirabox.notificationservice.channel.Channel
import java.time.Duration

class UpdatesChannel : Channel("Updates") {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val _apiUrl = "https://api.github.com/repos/shirabox/shirabox-app/releases/latest"
    private var releaseId: Int? = null

    override suspend fun listen() {
        coroutineScope {
            launch(Dispatchers.IO) {
                logger.info("Started listening GitHub API")

                while (true) {
                    logger.info("Checking for updates...")

                    /**
                     * Fetch the first asset id of the latest release
                     * and compare it with relevant asset id periodically
                     */

                    releaseId?.let {

                        val assetReleaseId = fetchLatestAssetId() ?: return@let
                        if(assetReleaseId > it) {
                            logger.info("Released new asset with id $assetReleaseId")
                            releaseId = assetReleaseId

                            this@UpdatesChannel.notifyObservers(
                                AppNotification(
                                    topic = "updates",
                                    "Доступно обновление!",
                                    "Список изменений смотреть в приложении."
                                )
                            )
                        }

                    } ?: launch {
                        // Init
                        fetchLatestAssetId()?.let {
                            logger.info("Fetched latest asset with id $it")
                            releaseId = it
                        }
                    }
                    delay(Duration.ofMinutes(30))
                }
            }
        }
    }

    private suspend fun fetchLatestAssetId(): Int? {
        val response = _apiUrl.httpGet()

        if(response.statusCode != 200) {
            logger.warn("(${response.statusCode}): Failed to fetch release data")
            return null
        }

        return try {
            val data = json.decodeFromString<GHubData>(response.body)
            data.assets.first().id
        } catch (e: Exception) {
            logger.error("Failed to serialize data: \n${e.localizedMessage}")
            null
        }
    }
}