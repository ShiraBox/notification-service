package live.shirabox.notificationservice.channel

import live.shirabox.notificationservice.AppNotification
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Channel(
    name: String
) : Observable {
    val logger: Logger = LoggerFactory.getLogger(name)

    private val _observers = mutableListOf<(AppNotification) -> Unit>()

    abstract suspend fun listen()
    override fun registerCallback(callback: (AppNotification) -> Unit) {
        _observers.add(callback)
    }

    override fun removeCallback(callback: (AppNotification) -> Unit) {
        _observers.remove(callback)
    }

    override fun notifyObservers(data: AppNotification) {
        _observers.forEach { it(data) }
    }
}