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
 * @property responseTimeMs Время ответа модели в миллисекундах (только для сообщений ассистента)
 * @property promptTokens Количество токенов в промпте (только для сообщений ассистента)
 * @property completionTokens Количество токенов в ответе (только для сообщений ассистента)
 * @property totalTokens Общее количество токенов (только для сообщений ассистента)
 */
@Serializable
data class Message(
    val id: String,
    val content: String,
    val timestamp: Long,
    val role: String,
    val responseTimeMs: Long? = null,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
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