package domain.usecase.settings

import data.repository.SettingsRepository

/**
 * Use Case для сброса настроек к значениям по умолчанию.
 *
 * @property settingsRepository Репозиторий настроек
 */
class ResetSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Сбрасывает все настройки к значениям по умолчанию
     *
     * @return Result<Unit> - успех или ошибка
     */
    operator fun invoke(): Result<Unit> {
        return try {
            settingsRepository.resetToDefaults()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}