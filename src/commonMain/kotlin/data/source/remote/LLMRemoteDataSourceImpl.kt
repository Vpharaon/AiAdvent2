package data.source.remote

import data.network.LLMApi
import data.network.model.ChatMessage
import data.network.model.ChatResponse

/**
 * Реализация удаленного источника данных для работы с LLM API.
 * Инкапсулирует работу с network слоем.
 *
 * @property llmApi API клиент для работы с LLM
 */
class LLMRemoteDataSourceImpl(
    private val llmApi: LLMApi
) : LLMRemoteDataSource {

    /**
     * Отправляет сообщения в LLM и получает ответ
     */
    override suspend fun sendMessages(
        messages: List<ChatMessage>,
        temperature: Double,
        maxTokens: Int?,
        apiUrl: String,
        modelName: String
    ): Result<ChatResponse> {
        return try {
            llmApi.sendMessage(
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens,
                apiUrl = apiUrl,
                modelName = modelName
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}