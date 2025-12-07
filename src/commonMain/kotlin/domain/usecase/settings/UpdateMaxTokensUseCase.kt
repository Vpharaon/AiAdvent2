package domain.usecase.settings

import data.repository.SettingsRepository

/**
 * Use Case для обновления максимального количества токенов.
 *
 * @property settingsRepository Репозиторий настроек
 */
class UpdateMaxTokensUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Обновляет максимальное количество токенов
     *
     * @param maxTokens Максимальное количество токенов (null для автоопределения)
     * @return Result<Unit> - успех или ошибка
     */
    operator fun invoke(maxTokens: Int?): Result<Unit> {
        return try {
            // Валидация значения
            if (maxTokens != null && maxTokens <= 0) {
                return Result.failure(
                    IllegalArgumentException("Количество токенов должно быть положительным числом")
                )
            }

            if (maxTokens != null && maxTokens > MAX_TOKENS_LIMIT) {
                return Result.failure(
                    IllegalArgumentException("Количество токенов не может превышать $MAX_TOKENS_LIMIT")
                )
            }

            settingsRepository.updateMaxTokens(maxTokens)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val MAX_TOKENS_LIMIT = 100000
    }
}