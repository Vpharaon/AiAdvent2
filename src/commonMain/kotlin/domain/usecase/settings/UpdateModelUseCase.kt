package domain.usecase.settings

import data.repository.SettingsRepository

/**
 * Use Case для обновления модели LLM в настройках.
 *
 * @property settingsRepository Репозиторий настроек
 */
class UpdateModelUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Обновляет модель LLM
     *
     * @param model Название модели
     * @return Result<Unit> - успех или ошибка
     */
    operator fun invoke(model: String): Result<Unit> {
        return try {
            if (model.isBlank()) {
                return Result.failure(IllegalArgumentException("Модель не может быть пустой"))
            }

            settingsRepository.updateModel(model)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}