package domain

import kotlinx.serialization.Serializable

/**
 * Доменная модель сообщения в чате.
 * Платформо-независимая модель, не содержит UI-логики.
 *
 * @property id Уникальный идентификатор сообщения
 * @property content Текст сообщения
 * @property timestamp Временная метка отправки сообщения (в миллисекундах с эпохи Unix)
 * @property role Роль отправителя сообщения (system, user, assistant)
 */
@Serializable
data class Message(
    val id: String,
    val content: String,
    val timestamp: Long,
    val role: String
) {
    /**
     * Проверяет, является ли сообщение от пользователя
     */
    val isUser: Boolean
        get() = role == "user"

    /**
     * Проверяет, является ли сообщение от ассистента
     */
    val isAssistant: Boolean
        get() = role == "assistant"

    /**
     * Проверяет, является ли сообщение системным
     */
    val isSystem: Boolean
        get() = role == "system"
}