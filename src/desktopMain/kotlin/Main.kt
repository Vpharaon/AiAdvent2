import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.*
import data.repository.SettingsRepository
import di.appModule
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
    // Загружаем local.properties
    val localProperties = loadLocalProperties()

    // Получаем GLM API ключ из local.properties или из переменной окружения
    val apiKey = localProperties.getProperty("glm.api.key")
        ?: System.getenv("GLM_API_KEY")
        ?: throw IllegalStateException(
            "GLM API key not found! Please set it in local.properties (glm.api.key) or as environment variable (GLM_API_KEY)"
        )

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val lifecycle = LifecycleRegistry()

    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Advent",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        KoinApplication(
            application = {
                modules(appModule(apiKey, appScope))
            }
        ) {
            val chatRepository = koinInject<data.repository.ChatRepository>()
            val structuredChatRepository = koinInject<data.repository.StructuredChatRepository>()
            val settingsRepository = koinInject<SettingsRepository>()

            val rootComponent = remember {
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    storeFactory = DefaultStoreFactory(),
                    chatRepository = chatRepository,
                    structuredChatRepository = structuredChatRepository,
                    settingsRepository = settingsRepository
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
                is RootComponent.Child.Home -> HomeScreen(child.component)
                is RootComponent.Child.Chat -> ChatScreen(child.component)
                is RootComponent.Child.Recipe -> RecipeScreen(child.component)
                is RootComponent.Child.EventPlanner -> EventPlanScreen(child.component)
                is RootComponent.Child.Settings -> SettingsScreen(child.component)
            }
        }
    }
}