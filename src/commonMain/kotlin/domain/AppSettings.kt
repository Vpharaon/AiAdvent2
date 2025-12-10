package domain

import kotlinx.serialization.Serializable

/**
 * Модель настроек приложения.
 *
 * Хранит пользовательские настройки, которые влияют на поведение AI моделей
 * и внешний вид приложения. Настройки сохраняются локально и восстанавливаются
 * при следующем запуске приложения.
 *
 * @property model Название модели LLM для использования (например, "glm-4.5-air")
 *                 @Deprecated Используйте selectedLlmModel вместо этого поля
 * @property selectedLlmModel Выбранная LLM модель со всеми параметрами (URL, API ключ, название).
 *                            По умолчанию используется бесплатная модель Mistral Devstral
 * @property temperature Температура для генерации текста (0.0 - 1.0).
 *                       - 0.0: Детерминированные, предсказуемые ответы
 *                       - 0.5: Сбалансированный режим
 *                       - 1.0: Креативные, разнообразные ответы (по умолчанию)
 * @property maxTokens Максимальное количество токенов в ответе модели.
 *                     null означает использование лимита по умолчанию от провайдера
 * @property theme Тема оформления приложения (светлая, темная или системная)
 *
 * @sample
 * ```kotlin
 * val customSettings = AppSettings(
 *     selectedLlmModel = LlmModels.GLM_4_6,
 *     temperature = 0.7,
 *     maxTokens = 2000,
 *     theme = Theme.DARK
 * )
 * ```
 */
@Serializable
data class AppSettings(
    @Deprecated("Use selectedLlmModel instead")
    val model: String = "glm-4.6",
    val selectedLlmModel: LlmModel = LlmModels.FREE_MISTRALAI_DEVSTRAL_2512,
    val temperature: Double = 1.0,
    val maxTokens: Int? = null,
    val theme: Theme = Theme.LIGHT
)

/**
 * Темы оформления приложения.
 *
 * Определяет визуальный стиль интерфейса приложения с использованием
 * Material Design 3 цветовых схем.
 */
@Serializable
enum class Theme {
    /**
     * Светлая тема.
     *
     * Использует светлые цвета фона и темный текст.
     * Подходит для работы в хорошо освещенных помещениях.
     */
    LIGHT,

    /**
     * Темная тема.
     *
     * Использует темные цвета фона и светлый текст.
     * Снижает нагрузку на глаза в условиях низкой освещенности
     * и экономит энергию на OLED экранах.
     */
    DARK,

    /**
     * Системная тема.
     *
     * Автоматически переключается между светлой и темной темой
     * в зависимости от системных настроек операционной системы.
     */
    SYSTEM
}