package data.network.model

import kotlinx.serialization.Serializable

/**
 * Основной класс для запроса к API чата.
 */
@Serializable
data class ChatRequest(
    // Название модели, которое вы хотите использовать.
    // Для вашего случая это "glm-4.5-flash".
    val model: String,
    // Список сообщений в чате, которые формируют контекст для модели.
    val messages: List<ChatMessage>,
    // Опциональные параметры для управления генерацией
    val temperature: Double? = 1.0, // Значение от 0.0 до 2.0. Более высокие значения делают ответ более случайным.
    val top_p: Double? = null,       // Альтернатива temperature. Ядро выборки.
    val max_tokens: Int? = null,     // Максимальное количество токенов в ответе.
    val stream: Boolean = false,     // Нужно ли возвращать ответ частями (потоково).
    val stop: List<String>? = null   // Список токенов, при встрече которых генерация остановится.
)
