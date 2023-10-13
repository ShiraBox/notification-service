package live.shirabox.notificationservice.channel.update

import kotlinx.serialization.Serializable

@Serializable
data class GHubData(
    val assets: List<GAsset>
)

@Serializable
data class GAsset(
    val id: Int
)
