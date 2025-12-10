package domain.service

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingType
import com.knuddels.jtokkit.api.ModelType
import domain.LlmModel

/**
 * Desktop-специфичная реализация подсчета токенов.
 * Использует библиотеку jtokkit для точного подсчета.
 *
 * Поддерживает:
 * - OpenAI модели (GPT-3.5, GPT-4, GPT-4o)
 * - Google Gemini (использует приближение через cl100k_base)
 * - DeepSeek (использует приближение через cl100k_base)
 * - Другие модели (fallback на cl100k_base)
 *
 * @property encodingRegistry Реестр энкодингов от jtokkit
 */
class TokenCounter : ITokenCounter {
    private val encodingRegistry = Encodings.newDefaultEncodingRegistry()

    /**
     * Подсчитывает количество токенов в тексте для указанной модели.
     * Реализация интерфейса ITokenCounter.
     *
     * @param text Текст для подсчета токенов
     * @param model LLM модель, для которой производится подсчет
     * @return Количество токенов
     */
    override fun countTokens(text: String, model: LlmModel): Int? {
        if (text.isBlank()) return 0

        val encoding = getEncodingForModel(model)
        return encoding.countTokens(text)
    }

    /**
     * Подсчитывает токены в списке сообщений с учетом служебных токенов.
     * Для чата учитываются дополнительные токены на форматирование сообщений.
     *
     * @param messages Список сообщений (текст)
     * @param model LLM модель
     * @return Общее количество токенов
     */
    fun countChatTokens(messages: List<String>, model: LlmModel): Int {
        val encoding = getEncodingForModel(model)

        // Базовое количество токенов для форматирования чата
        var totalTokens = 3 // Начальные токены

        messages.forEach { message ->
            // 4 токена на каждое сообщение (форматирование)
            totalTokens += 4
            totalTokens += encoding.countTokens(message)
        }

        // Дополнительные токены для ответа
        totalTokens += 3

        return totalTokens
    }

    /**
     * Получает encoding для конкретной модели.
     * Использует маппинг моделей на типы encoding.
     *
     * @param model LLM модель
     * @return Encoding для подсчета токенов
     */
    private fun getEncodingForModel(model: LlmModel): Encoding {
        return when {
            // OpenAI GPT-4o models
            model.modelName.contains("gpt-4o", ignoreCase = true) -> {
                encodingRegistry.getEncodingForModel(ModelType.GPT_4O)
            }

            // OpenAI GPT-4 models
            model.modelName.contains("gpt-4", ignoreCase = true) -> {
                encodingRegistry.getEncodingForModel(ModelType.GPT_4)
            }

            // OpenAI GPT-3.5 models
            model.modelName.contains("gpt-3.5", ignoreCase = true) -> {
                encodingRegistry.getEncodingForModel(ModelType.GPT_3_5_TURBO)
            }

            // OpenAI text-embedding models
            model.modelName.contains("text-embedding", ignoreCase = true) -> {
                encodingRegistry.getEncodingForModel(ModelType.TEXT_EMBEDDING_ADA_002)
            }

            // Google Gemini models - используем cl100k_base (похожий токенизатор)
            model.modelName.contains("gemini", ignoreCase = true) -> {
                encodingRegistry.getEncoding(EncodingType.CL100K_BASE)
            }

            // DeepSeek models - используем cl100k_base
            model.modelName.contains("deepseek", ignoreCase = true) -> {
                encodingRegistry.getEncoding(EncodingType.CL100K_BASE)
            }

            // Claude models - используем cl100k_base (приближение)
            model.modelName.contains("claude", ignoreCase = true) -> {
                encodingRegistry.getEncoding(EncodingType.CL100K_BASE)
            }

            // Llama models - используем cl100k_base (приближение)
            model.modelName.contains("llama", ignoreCase = true) -> {
                encodingRegistry.getEncoding(EncodingType.CL100K_BASE)
            }

            // Fallback - используем cl100k_base для всех остальных моделей
            else -> {
                encodingRegistry.getEncoding(EncodingType.CL100K_BASE)
            }
        }
    }

    /**
     * Оценивает, превышает ли текст лимит токенов для модели.
     *
     * @param text Текст для проверки
     * @param model LLM модель
     * @param limit Лимит токенов
     * @return true если текст превышает лимит
     */
    fun exceedsTokenLimit(text: String, model: LlmModel, limit: Int): Boolean {
        return (countTokens(text, model) ?: 0) > limit
    }

    /**
     * Обрезает текст до указанного количества токенов.
     * Полезно для укладывания в контекстное окно модели.
     *
     * @param text Исходный текст
     * @param model LLM модель
     * @param maxTokens Максимальное количество токенов
     * @return Обрезанный текст
     */
    fun truncateToTokenLimit(text: String, model: LlmModel, maxTokens: Int): String {
        val encoding = getEncodingForModel(model)
        val tokens = encoding.encode(text)

        // Подсчитываем токены через метод countTokens
        if (encoding.countTokens(text) <= maxTokens) {
            return text
        }

        // Берем первые maxTokens токенов из массива
        val tokenArray = tokens.toArray()
        val truncatedArray = tokenArray.take(maxTokens).toIntArray()

        // Создаем IntArrayList из обрезанного массива
        val truncatedTokens = com.knuddels.jtokkit.api.IntArrayList()
        truncatedArray.forEach { truncatedTokens.add(it) }

        return encoding.decode(truncatedTokens)
    }
}