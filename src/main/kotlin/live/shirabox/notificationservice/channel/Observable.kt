package live.shirabox.notificationservice.channel

import live.shirabox.notificationservice.AppNotification


interface Observable {
    fun registerCallback(callback: (AppNotification) -> Unit)
    fun removeCallback(callback: (AppNotification) -> Unit)
    fun notifyObservers(data: AppNotification)
}