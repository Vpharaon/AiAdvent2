package data.source.remote

import data.network.model.ChatMessage
import data.network.model.ChatResponse

/**
 * Интерфейс удаленного источника данных для работы с LLM API.
 * Абстракция над network слоем.
 */
interface LLMRemoteDataSource {
    /**
     * Отправляет сообщения в LLM и получает ответ
     *
     * @param messages Список сообщений для отправки
     * @param temperature Температура генерации (0.0 - 2.0)
     * @param maxTokens Максимальное количество токенов (null для автоопределения)
     * @return Result с ответом от LLM или ошибкой
     */
    suspend fun sendMessages(
        messages: List<ChatMessage>,
        temperature: Double,
        maxTokens: Int?
    ): Result<ChatResponse>
}