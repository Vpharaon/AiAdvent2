package data.network

import data.network.model.ChatMessage
import data.network.model.ChatResponse
import domain.ApiError

/**
 * Интерфейс для работы с LLM API.
 * Используется для тестируемости и возможности замены реализации.
 */
interface LLMApi {
    /**
     * Отправляет сообщение на LLM API и возвращает ответ.
     *
     * @param messages Список сообщений, составляющих диалог.
     * @param temperature Температура генерации (0.0 - 2.0)
     * @param maxTokens Максимальное количество токенов
     * @return [Result], содержащий либо [ChatResponse] в случае успеха,
     *         либо [ApiError] в случае ошибки.
     */
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        temperature: Double? = null,
        maxTokens: Int? = null
    ): Result<ChatResponse>

    /**
     * Закрывает соединение с API и освобождает ресурсы.
     */
    fun close()
}