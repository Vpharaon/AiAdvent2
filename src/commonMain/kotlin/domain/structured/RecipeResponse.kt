package domain.structured

import kotlinx.serialization.Serializable

/**
 * Структурированный ответ с рецептом от шеф-повара
 */
@Serializable
data class RecipeResponse(
    val name: String,
    val country: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val history: String
)

/**
 * Ингредиент в рецепте
 */
@Serializable
data class Ingredient(
    val name: String,
    val amount: String,
    val unit: String?
)

/**
 * Рецепт с raw JSON для отладки и демонстрации парсинга
 */
data class RecipeWithRaw(
    val recipe: RecipeResponse,
    val rawJson: String
)