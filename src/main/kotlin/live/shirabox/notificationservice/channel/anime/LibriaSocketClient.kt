package live.shirabox.notificationservice.channel.anime

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.URI

class LibriaSocketClient(
    uri: URI,
    private val reconnect: Boolean,
    val onConnect: (LibriaSocketClient) -> Unit,
    val onReceive: (String?) -> Unit
) : WebSocketClient(uri) {
    private val logger: Logger = LoggerFactory.getLogger("LibriaSocketClient")

    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.info("Connected to Libria Websocket")
        onConnect(this)
    }

    override fun onMessage(message: String?) {
        onReceive(message)
    }


    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        logger.warn("Connection closed" +
                "${if(remote) " by remote" else ""} with code $code ${reason?.let{" by reason: $reason"}}")

        if(reconnect) this.reconnect()
    }

    override fun onError(ex: Exception?) {
        logger.error("An error occurred: \n${ex?.localizedMessage}")
    }
}