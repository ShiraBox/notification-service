package live.shirabox.notificationservice.channel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object ChannelManager {
    private val _channels = mutableListOf<Channel>()

    fun registerChannels(vararg ch: Channel) {
        _channels.addAll(ch)
    }

    suspend fun runChannels() {
        coroutineScope {
            _channels.forEach {
                launch(Dispatchers.IO) { it.listen() }
            }
        }
    }
}