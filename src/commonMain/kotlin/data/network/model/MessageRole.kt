package data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Enum для ролей в чате. Использование Enum безопаснее, чем строки, так как помогает избежать опечаток.
 */
@Serializable
enum class MessageRole(val value: String) {
    @SerialName("system")
    SYSTEM("system"),
    @SerialName("user")
    USER("user"),
    @SerialName("assistant")
    ASSISTANT("assistant")
}