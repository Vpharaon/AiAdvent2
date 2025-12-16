package data.network

import data.network.model.ChatMessage
import data.network.model.ChatResponse
import data.network.model.Tool

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
     * @param apiUrl URL эндпоинта API
     * @param modelName Название модели для использования
     * @param tools Список доступных инструментов (функций) для LLM
     * @return [Result], содержащий либо [ChatResponse] в случае успеха,
     *         либо ошибку в случае неудачи.
     */
    suspend fun sendMessage(
        messages: List<ChatMessage>,
        temperature: Double? = null,
        maxTokens: Int? = null,
        apiUrl: String,
        modelName: String,
        tools: List<Tool>? = null
    ): Result<ChatResponse>

    /**
     * Закрывает соединение с API и освобождает ресурсы.
     */
    fun close()
}