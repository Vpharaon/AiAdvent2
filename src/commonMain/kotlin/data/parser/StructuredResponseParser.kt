package data.parser

import domain.structured.RecipeResponse
import domain.structured.RecipeWithRaw
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Парсер структурированных ответов от LLM
 */
class StructuredResponseParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Очищает ответ от markdown форматирования и лишнего текста
     */
    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()

        // Удаляем markdown блоки кода
        cleaned = cleaned.replace(Regex("```json\\s*"), "")
        cleaned = cleaned.replace(Regex("```\\s*"), "")

        // Ищем JSON объект между {}
        val jsonMatch = Regex("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}", RegexOption.DOT_MATCHES_ALL)
            .find(cleaned)

        if (jsonMatch != null) {
            cleaned = jsonMatch.value
        }

        return cleaned.trim()
    }

    /**
     * Парсит рецепт блюда
     */
    fun parseRecipe(response: String): Result<RecipeResponse> {
        return try {
            val cleaned = cleanJsonResponse(response)
            val result = json.decodeFromString<RecipeResponse>(cleaned)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Failed to parse recipe: ${e.message}", e))
        }
    }

    /**
     * Парсит рецепт блюда с сохранением raw JSON для отладки
     */
    fun parseRecipeWithRaw(rawResponse: String): Result<RecipeWithRaw> {
        return try {
            val cleaned = cleanJsonResponse(rawResponse)
            val recipe = json.decodeFromString<RecipeResponse>(cleaned)
            Result.success(
                RecipeWithRaw(
                    recipe = recipe,
                    rawJson = rawResponse,
                    fullResponseJson = "" // Будет заполнено в repository
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Failed to parse recipe: ${e.message}", e))
        }
    }
}