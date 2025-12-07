package domain.usecase.settings

import data.repository.SettingsRepository
import domain.Theme

/**
 * Use Case для обновления темы приложения.
 *
 * @property settingsRepository Репозиторий настроек
 */
class UpdateThemeUseCase(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Обновляет тему приложения
     *
     * @param theme Тема приложения
     * @return Result<Unit> - успех или ошибка
     */
    operator fun invoke(theme: Theme): Result<Unit> {
        return try {
            settingsRepository.updateTheme(theme)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}