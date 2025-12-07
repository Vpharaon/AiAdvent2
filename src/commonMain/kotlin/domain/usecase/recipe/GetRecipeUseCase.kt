package domain.usecase.recipe

import data.repository.StructuredChatRepository
import domain.structured.RecipeWithRaw

/**
 * Use Case для получения рецепта блюда.
 * Инкапсулирует бизнес-логику получения структурированного рецепта от шеф-повара.
 *
 * @property structuredChatRepository Репозиторий для структурированных запросов
 */
class GetRecipeUseCase(
    private val structuredChatRepository: StructuredChatRepository
) {
    /**
     * Получает рецепт блюда от шеф-повара
     *
     * @param dishName Название блюда
     * @return Result<RecipeWithRaw> - рецепт с raw JSON или ошибка
     */
    suspend operator fun invoke(dishName: String): Result<RecipeWithRaw> {
        // Валидация входных данных
        if (dishName.isBlank()) {
            return Result.failure(IllegalArgumentException("Название блюда не может быть пустым"))
        }

        if (dishName.length < MIN_DISH_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Название блюда слишком короткое (минимум $MIN_DISH_NAME_LENGTH символа)")
            )
        }

        if (dishName.length > MAX_DISH_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Название блюда слишком длинное (максимум $MAX_DISH_NAME_LENGTH символов)")
            )
        }

        // Получение рецепта через репозиторий
        return try {
            structuredChatRepository.getRecipeWithRaw(dishName.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val MIN_DISH_NAME_LENGTH = 2
        private const val MAX_DISH_NAME_LENGTH = 100
    }
}