package data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Основной класс-контейнер для ответа от API.
 */
@Serializable
data class ChatResponse(
    // Уникальный идентификатор запроса.
    val id: String? = null,

    // Время создания ответа в формате Unix timestamp.
    val created: Long? = null,

    // Модель, которая была использована для генерации ответа.
    val model: String? = null,

    // Список вариантов ответа. Обычно содержит один элемент.
    val choices: List<Choice>? = null,

    // Информация о использованных токенах.
    @SerialName("usage")
    val tokenUsage: Usage? = null
)


/**
 * Представляет один из вариантов ответа модели.
 */
@Serializable
data class Choice(
    // Порядковый номер варианта в списке.
    val index: Int? = null,

    // Сообщение, сгенерированное моделью.
    val message: ChatMessage? = null,

    // Причина, по которой генерация была завершена.
    // Возможные значения: "stop", "length", "content_filter", "function_call".
    @SerialName("finish_reason")
    val finishReason: String? = null
)

/**
 * Содержит информацию о количестве токенов, потраченных на запрос.
 */
@Serializable
data class Usage(
    // Количество токенов в промпте (запросе).
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,

    // Количество токенов в сгенерированном ответе (комплите).
    @SerialName("completion_tokens")
    val completionTokens: Int? = null,

    // Общее количество использованных токенов.
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)