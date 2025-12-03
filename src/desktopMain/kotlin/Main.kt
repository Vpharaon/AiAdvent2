import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.repository.SettingsRepository
import di.appModule
import domain.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import ui.screens.HomeScreen
import ui.screens.ChatScreen
import ui.screens.RecipeScreen
import ui.screens.EventPlanScreen
import ui.screens.SettingsScreen
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

    Window(
        onCloseRequest = ::exitApplication,
        title = "LLM Chat",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        KoinApplication(
            application = {
                modules(appModule(apiKey, appScope))
            }
        ) {
            App()
        }
    }
}

@Composable
fun App() {
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.settings.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()
    var showSettings by remember { mutableStateOf(false) }
    var showRecipes by remember { mutableStateOf(false) }
    var showEventPlanner by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }

    val useDarkTheme = when (settings.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = if (useDarkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        when {
            showSettings -> {
                SettingsScreen(
                    onBack = { showSettings = false }
                )
            }
            showRecipes -> {
                RecipeScreen(
                    onBack = { showRecipes = false }
                )
            }
            showEventPlanner -> {
                EventPlanScreen(
                    onBack = { showEventPlanner = false }
                )
            }
            showChat -> {
                ChatScreen(
                    onBack = { showChat = false },
                    onOpenSettings = { showSettings = true }
                )
            }
            else -> {
                HomeScreen(
                    onOpenEventPlanner = { showEventPlanner = true },
                    onOpenRecipes = { showRecipes = true },
                    onOpenChat = { showChat = true }
                )
            }
        }
    }
}