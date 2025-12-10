package data.repository

import domain.AppSettings
import domain.LlmModel
import domain.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Интерфейс репозитория для управления настройками приложения.
 *
 * Предоставляет централизованный доступ к пользовательским настройкам,
 * включая выбор LLM модели, параметры генерации и тему оформления.
 * Использует StateFlow для реактивного обновления UI при изменении настроек.
 */
interface SettingsRepository {
    /**
     * Реактивный поток текущих настроек приложения.
     *
     * Подписчики автоматически получают обновления при любом изменении настроек.
     * Используется для синхронизации UI с текущим состоянием настроек.
     */
    val settings: StateFlow<AppSettings>

    /**
     * Обновляет выбранную LLM модель.
     *
     * Изменяет модель, используемую для генерации ответов. Модель содержит
     * все необходимые параметры: API URL, имя модели, ключ доступа.
     *
     * @param model Полная модель LLM для использования
     */
    fun updateSelectedLlmModel(model: LlmModel)

    /**
     * Обновляет температуру генерации текста.
     *
     * Температура контролирует случайность ответов модели:
     * - 0.0: Детерминированные, предсказуемые ответы
     * - 1.0: Более креативные и разнообразные ответы
     * - 2.0: Максимальная креативность (может быть менее связным)
     *
     * @param temperature Значение температуры в диапазоне [0.0, 2.0]
     */
    fun updateTemperature(temperature: Double)

    /**
     * Обновляет максимальное количество токенов в ответе.
     *
     * Ограничивает длину генерируемого ответа. Null означает использование
     * лимита по умолчанию от провайдера модели.
     *
     * @param maxTokens Максимальное количество токенов или null для значения по умолчанию
     */
    fun updateMaxTokens(maxTokens: Int?)

    /**
     * Обновляет тему оформления приложения.
     *
     * Поддерживаемые темы:
     * - LIGHT: Светлая тема для дневного использования
     * - DARK: Темная тема для ночного использования
     * - SYSTEM: Автоматический выбор по системным настройкам
     *
     * @param theme Выбранная тема
     */
    fun updateTheme(theme: Theme)

    /**
     * Сбрасывает все настройки к значениям по умолчанию.
     *
     * Восстанавливает:
     * - Модель по умолчанию (Mistral Devstral)
     * - Температуру 1.0
     * - Максимум токенов null (без ограничений)
     * - Светлую тему
     */
    fun resetToDefaults()

    /**
     * Возвращает текущий снимок настроек.
     *
     * В отличие от [settings] StateFlow, этот метод возвращает значение
     * напрямую без подписки на изменения.
     *
     * @return Текущие настройки приложения
     */
    fun getCurrentSettings(): AppSettings
}

/**
 * Реализация репозитория для управления настройками приложения.
 *
 * Текущая реализация:
 * - Хранит настройки в памяти с использованием MutableStateFlow
 * - Настройки сбрасываются при перезапуске приложения
 * - Обеспечивает реактивное обновление всех подписчиков при изменениях
 *
 * Ограничения:
 * - Настройки не сохраняются между сессиями
 *
 * Планы на улучшение:
 * - TODO: Добавить сохранение в файл/DataStore для персистентности настроек
 * - TODO: Добавить валидацию при обновлении настроек
 * - TODO: Поддержка миграции настроек между версиями
 */
class SettingsRepositoryImpl : SettingsRepository {
    // Изменяемый поток настроек для внутреннего использования
    private val _settings = MutableStateFlow(AppSettings())

    // Неизменяемый поток для внешнего доступа (read-only)
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * Обновляет выбранную LLM модель.
     *
     * Создает новый immutable снимок настроек с обновленной моделью.
     *
     * @param model Новая модель для использования
     */
    override fun updateSelectedLlmModel(model: LlmModel) {
        _settings.value = _settings.value.copy(selectedLlmModel = model)
    }

    /**
     * Обновляет температуру генерации с валидацией диапазона.
     *
     * Автоматически ограничивает значение в диапазоне [0.0, 2.0]
     * для предотвращения некорректных значений.
     *
     * @param temperature Желаемая температура (будет ограничена диапазоном [0.0, 2.0])
     */
    override fun updateTemperature(temperature: Double) {
        _settings.value = _settings.value.copy(
            temperature = temperature.coerceIn(0.0, 2.0)
        )
    }

    /**
     * Обновляет максимальное количество токенов в ответе.
     *
     * @param maxTokens Максимум токенов или null для снятия ограничения
     */
    override fun updateMaxTokens(maxTokens: Int?) {
        _settings.value = _settings.value.copy(maxTokens = maxTokens)
    }

    /**
     * Обновляет тему оформления приложения.
     *
     * Изменение темы мгновенно отражается во всех подписанных компонентах UI.
     *
     * @param theme Новая тема для применения
     */
    override fun updateTheme(theme: Theme) {
        _settings.value = _settings.value.copy(theme = theme)
    }

    /**
     * Сбрасывает все настройки к значениям по умолчанию из AppSettings().
     *
     * Восстанавливает:
     * - Модель: Mistral Devstral (бесплатная)
     * - Температура: 1.0
     * - Максимум токенов: null (без ограничений)
     * - Тема: LIGHT
     */
    override fun resetToDefaults() {
        _settings.value = AppSettings()
    }

    /**
     * Возвращает текущий snapshot настроек.
     *
     * Этот метод полезен для получения разового значения настроек
     * без создания подписки на StateFlow.
     *
     * @return Текущие настройки приложения
     */
    override fun getCurrentSettings(): AppSettings = _settings.value
}