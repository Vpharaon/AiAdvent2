import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import java.io.File
import java.io.FileInputStream
import java.util.Properties

fun loadLocalProperties(): Properties {
    val properties = Properties()
    val localPropertiesFile = File("local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { properties.load(it) }
    }
    return properties
}

fun main() = application {
    // Инициализируем MVIKotlin main thread
    setMainThreadId(Thread.currentThread().id)

    // Загружаем local.properties
    val localProperties = loadLocalProperties()

    // Получаем GLM API ключ из local.properties или из переменной окружения
    val glmApiKey = localProperties.getProperty("glm.api.key")
        ?: System.getenv("GLM_API_KEY")
        ?: throw IllegalStateException(
            "GLM API key not found! Please set it in local.properties (glm.api.key) or as environment variable (GLM_API_KEY)"
        )

    // Получаем OpenRouter API ключ из local.properties или из переменной окружения
    val openRouterApiKey = localProperties.getProperty("openrouter.api.key")
        ?: System.getenv("OPENROUTER_API_KEY")
        ?: ""

    // Получаем OpenAI API ключ из local.properties или из переменной окружения
    val openAiApiKey = localProperties.getProperty("openai.api.key")
        ?: System.getenv("OPENAI_API_KEY")
        ?: ""

    // Получаем DeepSeek API ключ из local.properties или из переменной окружения
    val deepSeekApiKey = localProperties.getProperty("deepseek.api.key")
        ?: System.getenv("DEEPSEEK_API_KEY")
        ?: ""

    // Создаем Map с ключами для разных провайдеров
    val apiKeys = mapOf(
        "glm" to glmApiKey,
        "openrouter" to openRouterApiKey,
        "openai" to openAiApiKey,
        "deepseek" to deepSeekApiKey
    )

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val lifecycle = LifecycleRegistry()

    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Advent",
        state = rememberWindowState(
            width = 1600.dp,
            height = 900.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        KoinApplication(
            application = {
                modules(appModule(apiKeys, appScope), desktopModule)
            }
        ) {
            // Repositories
            val chatRepository = koinInject<data.repository.ChatRepository>()
            val settingsRepository = koinInject<SettingsRepository>()

            // Chat Use Cases
            val sendMessageUseCase = koinInject<domain.usecase.chat.SendMessageUseCase>()
            val sendSystemPromptUseCase = koinInject<domain.usecase.chat.SendSystemPromptUseCase>()
            val clearChatUseCase = koinInject<domain.usecase.chat.ClearChatUseCase>()

            // Settings Use Cases
            val updateThemeUseCase = koinInject<domain.usecase.settings.UpdateThemeUseCase>()
            val updateTemperatureUseCase = koinInject<domain.usecase.settings.UpdateTemperatureUseCase>()
            val updateMaxTokensUseCase = koinInject<domain.usecase.settings.UpdateMaxTokensUseCase>()
            val resetSettingsUseCase = koinInject<domain.usecase.settings.ResetSettingsUseCase>()

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

            App(rootComponent)
        }
    }
}

@Composable
fun App(rootComponent: RootComponent) {
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.settings.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()

    val useDarkTheme = when (settings.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = if (useDarkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        val stack by rootComponent.stack.subscribeAsState()

        Children(
            stack = stack
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.Chat -> ChatScreen(child.component)
                is RootComponent.Child.Settings -> SettingsScreen(child.component)
            }
        }
    }
}