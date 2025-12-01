package domain

import kotlinx.serialization.Serializable

/**
 * Доменная модель сообщения в чате.
 *
 * @property id Уникальный идентификатор сообщения
 * @property content Текст сообщения
 * @property timestamp Временная метка отправки сообщения (в миллисекундах с эпохи Unix)
 */
@Serializable
data class Message(
    val id: String,
    val content: String,
    val timestamp: Long,
    val role: String
)