import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.utils.setMainThreadId
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.*
import data.repository.SettingsRepository
import di.appModule
import di.desktopModule
import domain.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import ui.screens.*
import ui.theme.AppTheme
import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 * Загружает локальные настройки из файла local.properties.
 *
 * Этот файл используется для хранения конфиденциальных данных (API ключи)
 * и не должен попадать в систему контроля версий (добавлен в .gitignore).
 *
 * @return Properties объект с загруженными настройками или пустой Properties,
 *         если файл не существует
 */
fun loadLocalProperties(): Properties {
    val properties = Properties()
    val localPropertiesFile = File("local.properties")

    // Проверяем существование файла перед загрузкой
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { inputStream ->
            properties.load(inputStream)
        }
    }

    return properties
}

/**
 * Главная точка входа в приложение AI Advent.
 *
 * Инициализирует:
 * - MVIKotlin для управления состоянием
 * - API ключи для различных провайдеров LLM
 * - Koin для внедрения зависимостей
 * - Decompose для навигации между экранами
 *
 * API ключи загружаются в следующем порядке приоритета:
 * 1. Из файла local.properties
 * 2. Из переменных окружения
 * 3. Пустая строка (для опциональных ключей)
 */
fun main() = application {
    // Инициализируем MVIKotlin для корректной работы на Desktop платформе
    // Это необходимо для синхронизации обновлений состояния с главным потоком
    setMainThreadId(Thread.currentThread().id)

    // Загружаем локальные настройки из файла local.properties
    val localProperties = loadLocalProperties()

    /**
     * Получение API ключа для GLM (обязательный).
     * GLM - основной провайдер моделей в приложении.
     * Порядок поиска: local.properties -> переменная окружения -> ошибка
     */
    val glmApiKey = localProperties.getProperty("glm.api.key")
        ?: System.getenv("GLM_API_KEY")
        ?: throw IllegalStateException(
            "GLM API key not found! Please set it in local.properties (glm.api.key) " +
            "or as environment variable (GLM_API_KEY)"
        )

    /**
     * Получение API ключа для OpenRouter (опциональный).
     * OpenRouter - агрегатор различных LLM моделей.
     * Порядок поиска: local.properties -> переменная окружения -> пустая строка
     */
    val openRouterApiKey = localProperties.getProperty("openrouter.api.key")
        ?: System.getenv("OPENROUTER_API_KEY")
        ?: ""

    /**
     * Получение API ключа для OpenAI (опциональный).
     * OpenAI - провайдер GPT моделей.
     * Порядок поиска: local.properties -> переменная окружения -> пустая строка
     */
    val openAiApiKey = localProperties.getProperty("openai.api.key")
        ?: System.getenv("OPENAI_API_KEY")
        ?: ""

    /**
     * Получение API ключа для DeepSeek (опциональный).
     * DeepSeek - провайдер специализированных моделей для кода.
     * Порядок поиска: local.properties -> переменная окружения -> пустая строка
     */
    val deepSeekApiKey = localProperties.getProperty("deepseek.api.key")
        ?: System.getenv("DEEPSEEK_API_KEY")
        ?: ""

    /**
     * Создаем карту с API ключами для всех поддерживаемых провайдеров.
     * Эта карта будет использована при инициализации DI контейнера.
     */
    val apiKeys = mapOf(
        "glm" to glmApiKey,
        "openrouter" to openRouterApiKey,
        "openai" to openAiApiKey,
        "deepseek" to deepSeekApiKey
    )

    // Создаем корутинный scope для фоновых операций приложения
    // SupervisorJob обеспечивает независимость задач друг от друга
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Создаем registry жизненного цикла для Decompose компонентов
    val lifecycle = LifecycleRegistry()

    // Создаем главное окно приложения
    Window(
        onCloseRequest = ::exitApplication,  // Закрытие приложения при закрытии окна
        title = "AI Advent",                 // Заголовок окна
        state = rememberWindowState(
            width = 1600.dp,                  // Ширина окна
            height = 900.dp,                  // Высота окна
            position = WindowPosition(Alignment.Center)  // Центрирование на экране
        )
    ) {
        /**
         * Инициализация Koin - библиотеки для внедрения зависимостей.
         * Регистрируем модули с репозиториями, use cases и другими компонентами.
         */
        KoinApplication(
            application = {
                modules(
                    appModule(apiKeys, appScope),  // Основной модуль приложения
                    desktopModule                  // Desktop-специфичный модуль
                )
            }
        ) {
            /**
             * Инжектим репозитории через Koin.
             * Репозитории отвечают за работу с данными (API, локальное хранилище).
             */
            val chatRepository = koinInject<data.repository.ChatRepository>()
            val settingsRepository = koinInject<SettingsRepository>()

            /**
             * Инжектим Use Cases для работы с чатом.
             * Use Cases инкапсулируют бизнес-логику приложения.
             */
            val sendMessageUseCase = koinInject<domain.usecase.chat.SendMessageUseCase>()
            val sendSystemPromptUseCase = koinInject<domain.usecase.chat.SendSystemPromptUseCase>()
            val clearChatUseCase = koinInject<domain.usecase.chat.ClearChatUseCase>()

            /**
             * Инжектим Use Cases для работы с настройками.
             */
            val updateThemeUseCase = koinInject<domain.usecase.settings.UpdateThemeUseCase>()
            val updateTemperatureUseCase = koinInject<domain.usecase.settings.UpdateTemperatureUseCase>()
            val updateMaxTokensUseCase = koinInject<domain.usecase.settings.UpdateMaxTokensUseCase>()
            val resetSettingsUseCase = koinInject<domain.usecase.settings.ResetSettingsUseCase>()

            /**
             * Создаем корневой компонент навигации.
             * Используем remember для сохранения компонента при рекомпозиции.
             */
            val rootComponent = remember {
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    storeFactory = DefaultStoreFactory(),
                    chatRepository = chatRepository,
                    settingsRepository = settingsRepository,
                    sendMessageUseCase = sendMessageUseCase,
                    sendSystemPromptUseCase = sendSystemPromptUseCase,
                    clearChatUseCase = clearChatUseCase,
                    updateThemeUseCase = updateThemeUseCase,
                    updateTemperatureUseCase = updateTemperatureUseCase,
                    updateMaxTokensUseCase = updateMaxTokensUseCase,
                    resetSettingsUseCase = resetSettingsUseCase
                )
            }

            // Запускаем главный UI компонент приложения
            App(rootComponent)
        }
    }
}

/**
 * Главный composable компонент приложения.
 *
 * Отвечает за:
 * - Применение темы оформления (светлая/темная/системная)
 * - Управление навигацией между экранами через Decompose
 * - Отображение соответствующих экранов на основе текущего состояния навигации
 *
 * @param rootComponent Корневой компонент навигации, управляющий стеком экранов
 */
@Composable
fun App(rootComponent: RootComponent) {
    // Получаем репозиторий настроек через Koin
    val settingsRepository: SettingsRepository = koinInject()

    // Подписываемся на изменения настроек
    val settings by settingsRepository.settings.collectAsState()

    // Определяем, использует ли система темную тему
    val systemInDarkTheme = isSystemInDarkTheme()

    /**
     * Определяем, какую тему использовать, на основе настроек пользователя:
     * - Theme.LIGHT - всегда светлая тема
     * - Theme.DARK - всегда темная тема
     * - Theme.SYSTEM - следуем за системными настройками
     */
    val useDarkTheme = when (settings.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> systemInDarkTheme
    }

    /**
     * Применяем нашу кастомную тему с красивой палитрой цветов.
     * Тема автоматически переключается между светлой и темной в зависимости
     * от настроек пользователя или системных предпочтений.
     */
    AppTheme(darkTheme = useDarkTheme) {
        // Получаем текущий стек навигации
        val stack by rootComponent.stack.subscribeAsState()

        /**
         * Children - компонент Decompose для отображения активного экрана из стека.
         * Обеспечивает плавные переходы между экранами и сохранение их состояния.
         */
        Children(
            stack = stack
        ) {
            // Отображаем соответствующий экран в зависимости от типа child компонента
            when (val child = it.instance) {
                is RootComponent.Child.Chat -> ChatScreen(child.component)
                is RootComponent.Child.Settings -> SettingsScreen(child.component)
            }
        }
    }
}