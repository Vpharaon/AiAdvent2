package data.network.model

import kotlinx.serialization.Serializable

/**
 * Представляет одно сообщение в чате.
 */
@Serializable
data class ChatMessage(
    // Роль автора сообщения: "system", "user" или "assistant".
    val role: MessageRole? = null,
    // Содержимое сообщения.
    val content: String? = null
)