package domain

import kotlinx.serialization.Serializable

/**
 * Модель настроек приложения.
 *
 * @property model Название модели LLM для использования (например, "glm-4.5-air")
 * @property temperature Температура для генерации (0.0 - 2.0). Более высокие значения делают ответ более случайным
 * @property maxTokens Максимальное количество токенов в ответе
 * @property theme Тема оформления приложения
 */
@Serializable
data class AppSettings(
    val model: String = "glm-4.5-air",
    val temperature: Double = 1.0,
    val maxTokens: Int? = null,
    val theme: Theme = Theme.LIGHT
)

/**
 * Темы оформления приложения.
 */
@Serializable
enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}