package data.repository

import data.network.LLMApi
import data.network.model.ChatMessage
import data.network.model.ChatResponse
import data.network.model.MessageRole
import data.parser.StructuredResponseParser
import data.prompt.StructuredPromptBuilder
import domain.structured.RecipeResponse
import domain.structured.RecipeWithRaw
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Репозиторий для работы со структурированными запросами к LLM
 */
class StructuredChatRepository(
    private val llmApiClient: LLMApi,
    private val settingsRepository: SettingsRepository
) {
    private val promptBuilder = StructuredPromptBuilder()
    private val parser = StructuredResponseParser()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

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
                content = "You are a world-class chef. Answer ONLY in JSON format, without additional text."
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
                content = "You are a world-class chef. Answer ONLY in JSON format, without additional text."
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
                    // Сериализуем полный ответ от API
                    val fullResponseJson = json.encodeToString<ChatResponse>(chatResponse)

                    // Парсим рецепт и добавляем полный ответ
                    parser.parseRecipeWithRaw(content).map { recipeWithRaw ->
                        recipeWithRaw.copy(fullResponseJson = fullResponseJson)
                    }
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