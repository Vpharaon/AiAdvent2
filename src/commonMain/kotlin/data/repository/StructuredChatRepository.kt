package data.repository

import data.network.model.ChatMessage
import data.network.model.ChatResponse
import data.network.model.MessageRole
import data.source.remote.LLMRemoteDataSource
import domain.util.StructuredResponseParser
import domain.util.StructuredPromptBuilder
import domain.structured.RecipeResponse
import domain.structured.RecipeWithRaw
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Интерфейс репозитория для работы со структурированными запросами к LLM
 */
interface StructuredChatRepository {
    /**
     * Получает структурированный рецепт блюда от шеф-повара
     *
     * @param dishName Название блюда
     * @return Result с RecipeResponse или ошибкой
     */
    suspend fun getRecipe(dishName: String): Result<RecipeResponse>

    /**
     * Получает структурированный рецепт блюда с сохранением raw и cleaned JSON
     *
     * @param dishName Название блюда
     * @return Result с RecipeWithRaw (рецепт + raw JSON + cleaned JSON) или ошибкой
     */
    suspend fun getRecipeWithRaw(dishName: String): Result<RecipeWithRaw>
}

/**
 * Реализация репозитория для работы со структурированными запросами к LLM.
 * Использует DataSource для абстракции от деталей сети.
 */
class StructuredChatRepositoryImpl(
    private val remoteDataSource: LLMRemoteDataSource,
    private val settingsRepository: SettingsRepository
) : StructuredChatRepository {
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
    override suspend fun getRecipe(dishName: String): Result<RecipeResponse> {
        return fetchRecipe(dishName).fold(
            onSuccess = { (content, _) ->
                parser.parseRecipe(content)
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
    override suspend fun getRecipeWithRaw(dishName: String): Result<RecipeWithRaw> {
        return fetchRecipe(dishName).fold(
            onSuccess = { (content, chatResponse) ->
                // Сериализуем полный ответ от API
                val fullResponseJson = json.encodeToString<ChatResponse>(chatResponse)

                // Парсим рецепт и добавляем полный ответ
                parser.parseRecipeWithRaw(content).map { recipeWithRaw ->
                    recipeWithRaw.copy(fullResponseJson = fullResponseJson)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    /**
     * Общий метод для получения рецепта от LLM.
     * Устраняет дублирование кода между getRecipe и getRecipeWithRaw.
     *
     * @param dishName Название блюда
     * @return Result с парой (контент ответа, полный ChatResponse)
     */
    private suspend fun fetchRecipe(dishName: String): Result<Pair<String, ChatResponse>> {
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
        val selectedModel = settings.selectedLlmModel
        val response = remoteDataSource.sendMessages(
            messages = messages,
            temperature = 0.3, // Низкая температура для более детерминированных ответов
            maxTokens = settings.maxTokens,
            apiUrl = selectedModel.apiUrl,
            modelName = selectedModel.modelName
        )

        return response.fold(
            onSuccess = { chatResponse ->
                val content = chatResponse.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    Result.success(content to chatResponse)
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