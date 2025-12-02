package data.repository

import data.network.LLMApi
import data.network.model.ChatMessage
import data.network.model.MessageRole
import data.parser.StructuredResponseParser
import data.prompt.StructuredPromptBuilder
import domain.structured.RecipeResponse
import domain.structured.RecipeWithRaw

/**
 * Репозиторий для работы со структурированными запросами к LLM
 */
class StructuredChatRepository(
    private val llmApiClient: LLMApi,
    private val settingsRepository: SettingsRepository
) {
    private val promptBuilder = StructuredPromptBuilder()
    private val parser = StructuredResponseParser()

    /**
     * Получает структурированный рецепт блюда от шеф-повара
     *
     * @param dishName Название блюда
     * @return Result с RecipeResponse или ошибкой
     */
    suspend fun getRecipe(dishName: String): Result<RecipeResponse> {
        val prompt = promptBuilder.buildRecipePrompt(dishName)

        val messages = listOf(
            ChatMessage(
                role = MessageRole.SYSTEM,
                content = "Ты - шеф-повар мирового класса. Отвечай ТОЛЬКО в формате JSON без дополнительного текста."
            ),
            ChatMessage(
                role = MessageRole.USER,
                content = prompt
            )
        )

        val settings = settingsRepository.getCurrentSettings()
        val response = llmApiClient.sendMessage(
            messages = messages,
            temperature = 0.3, // Низкая температура для более детерминированных ответов
            maxTokens = settings.maxTokens
        )

        return response.fold(
            onSuccess = { chatResponse ->
                val content = chatResponse.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    parser.parseRecipe(content)
                } else {
                    Result.failure(IllegalStateException("Empty response from LLM"))
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Получает структурированный рецепт блюда с сохранением raw и cleaned JSON
     *
     * @param dishName Название блюда
     * @return Result с RecipeWithRaw (рецепт + raw JSON + cleaned JSON) или ошибкой
     */
    suspend fun getRecipeWithRaw(dishName: String): Result<RecipeWithRaw> {
        val prompt = promptBuilder.buildRecipePrompt(dishName)

        val messages = listOf(
            ChatMessage(
                role = MessageRole.SYSTEM,
                content = "Ты - шеф-повар мирового класса. Отвечай ТОЛЬКО в формате JSON без дополнительного текста."
            ),
            ChatMessage(
                role = MessageRole.USER,
                content = prompt
            )
        )

        val settings = settingsRepository.getCurrentSettings()
        val response = llmApiClient.sendMessage(
            messages = messages,
            temperature = 0.3, // Низкая температура для более детерминированных ответов
            maxTokens = settings.maxTokens
        )

        return response.fold(
            onSuccess = { chatResponse ->
                val content = chatResponse.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    parser.parseRecipeWithRaw(content)
                } else {
                    Result.failure(IllegalStateException("Empty response from LLM"))
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}