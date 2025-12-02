package data.repository

import domain.AppSettings
import domain.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Репозиторий для управления настройками приложения.
 * В текущей реализации настройки хранятся в памяти.
 * TODO: Добавить сохранение в файл/DataStore для персистентности.
 */
class SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * Обновляет настройки модели LLM.
     */
    fun updateModel(model: String) {
        _settings.value = _settings.value.copy(model = model)
    }

    /**
     * Обновляет температуру генерации.
     */
    fun updateTemperature(temperature: Double) {
        _settings.value = _settings.value.copy(
            temperature = temperature.coerceIn(0.0, 2.0)
        )
    }

    /**
     * Обновляет максимальное количество токенов.
     */
    fun updateMaxTokens(maxTokens: Int?) {
        _settings.value = _settings.value.copy(maxTokens = maxTokens)
    }

    /**
     * Обновляет тему приложения.
     */
    fun updateTheme(theme: Theme) {
        _settings.value = _settings.value.copy(theme = theme)
    }

    /**
     * Сбрасывает настройки к значениям по умолчанию.
     */
    fun resetToDefaults() {
        _settings.value = AppSettings()
    }

    /**
     * Возвращает текущие настройки.
     */
    fun getCurrentSettings(): AppSettings = _settings.value
}