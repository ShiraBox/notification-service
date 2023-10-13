package live.shirabox.notificationservice

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import live.shirabox.notificationservice.channel.ChannelManager
import live.shirabox.notificationservice.channel.anime.LibriaChannel
import live.shirabox.notificationservice.channel.update.UpdatesChannel
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Default")

suspend fun main() = coroutineScope {
    val key = System.getenv("FIREBASE_KEY") ?: "null"
    Firebase.authenticate(key)

    logger.info("Loading notification channels...")
    ChannelManager.registerChannels(
        UpdatesChannel().apply {
            registerCallback {
                Firebase.sendNotification(it)
            }
        },
        LibriaChannel().apply {
            registerCallback {
                Firebase.sendNotification(it)
            }
        }
    )

    launch { ChannelManager.runChannels() }

    return@coroutineScope
}