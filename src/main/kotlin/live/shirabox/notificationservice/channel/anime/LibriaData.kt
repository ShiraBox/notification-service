package live.shirabox.notificationservice.channel.anime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RootWrapper<T> (
    val type: String,
    val data: T
)

@Serializable
data class SocketInitMessage(
    val connection: String,
    @SerialName("api_version") val apiVersion: String
)

@Serializable
data class EncodeFinishData(
    val id: Int,
    val episode: Int
)

@Serializable
data class LibriaAnimeData(
    val code: String,
    val names: LibriaNames
)

@Serializable
data class LibriaNames(
    val en: String,
    val ru: String
)

@Serializable
data class Subscribe(
    val subscribe: SubscriptionType
)

@Serializable
data class SubscriptionType(
    val type: String
)

@Serializable
data class SubscriptionResponse(
    val subscribe: String
)
