package live.shirabox.notificationservice

data class AppNotification(
    val topic: String,
    val title: String,
    val text: String,
    val data: Map<String, String>? = null
)
