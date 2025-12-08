package domain.usecase.settings

import data.repository.SettingsRepository

/**
 * Use Case для обновления температуры генерации.
 *
 * @property settingsRepository Репозиторий настроек
 */
class UpdateTemperatureUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Обновляет температуру генерации
     *
     * @param temperature Температура (0.0 - 1.0)
     * @return Result<Unit> - успех или ошибка
     */
    operator fun invoke(temperature: Double): Result<Unit> {
        return try {
            // Валидация диапазона
            if (temperature !in MIN_TEMPERATURE..MAX_TEMPERATURE) {
                return Result.failure(
                    IllegalArgumentException("Температура должна быть в диапазоне $MIN_TEMPERATURE-$MAX_TEMPERATURE")
                )
            }

            settingsRepository.updateTemperature(temperature)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val MIN_TEMPERATURE = 0.0
        private const val MAX_TEMPERATURE = 1.0
    }
}