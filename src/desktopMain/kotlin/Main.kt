import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
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
import domain.Message
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import viewmodel.ChatViewModel
import java.io.File
import java.io.FileInputStream
import java.util.Properties
import ui.MarkdownText

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

    val useDarkTheme = when (settings.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = if (useDarkTheme) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        if (showSettings) {
            SettingsScreen(
                onBack = { showSettings = false }
            )
        } else {
            ChatScreen(
                onOpenSettings = { showSettings = true }
            )
        }
    }
}

@Composable
fun ChatScreen(onOpenSettings: () -> Unit) {
    val viewModel: ChatViewModel = koinInject()
    val messages by viewModel.messages.collectAsState()
    val input by viewModel.input.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой настроек
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LLM Chat",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onOpenSettings) {
                Text("⚙️", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Область сообщений
        MessagesArea(
            messages = messages,
            isTyping = isTyping,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Область ввода и кнопок
        InputArea(
            input = input,
            onInputChange = { viewModel.updateInput(it) },
            onSendClick = { viewModel.sendMessage() },
            onClearClick = { viewModel.clearChat() }
        )
    }
}

@Composable
fun MessagesArea(
    messages: List<Message>,
    isTyping: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Автопрокрутка вниз при добавлении новых сообщений или изменении isTyping
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty() || isTyping) {
            listState.animateScrollToItem(if (isTyping) messages.size else messages.size - 1)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message = message)
            }

            // Индикатор "Bot is typing..."
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bot is typing",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val backgroundColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clickable {
                    // Копирование текста в буфер обмена
                    val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                    val stringSelection = java.awt.datatransfer.StringSelection(message.content)
                    clipboard.setContents(stringSelection, null)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                MarkdownText(
                    markdown = message.content,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.formattedTime,
                    color = textColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun InputArea(
    input: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Поле ввода сообщения
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
                        if (!keyEvent.isShiftPressed) {
                            onSendClick()
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                },
            placeholder = { Text("Введите сообщение...") },
            maxLines = 4
        )

        // Кнопка Send
        Button(
            onClick = onSendClick,
            modifier = Modifier.height(56.dp),
            enabled = input.trim().isNotEmpty()
        ) {
            Text("Send")
        }

        // Кнопка Clear Chat
        OutlinedButton(
            onClick = onClearClick,
            modifier = Modifier.height(56.dp)
        ) {
            Text("Clear Chat")
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой назад
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.titleLarge)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Выбор темы
                Text(
                    text = "Тема",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.theme == Theme.LIGHT,
                        onClick = { settingsRepository.updateTheme(Theme.LIGHT) },
                        label = { Text("Светлая") }
                    )
                    FilterChip(
                        selected = settings.theme == Theme.DARK,
                        onClick = { settingsRepository.updateTheme(Theme.DARK) },
                        label = { Text("Тёмная") }
                    )
                    FilterChip(
                        selected = settings.theme == Theme.SYSTEM,
                        onClick = { settingsRepository.updateTheme(Theme.SYSTEM) },
                        label = { Text("Системная") }
                    )
                }

                Divider()

                // Температура
                Text(
                    text = "Температура: ${String.format("%.2f", settings.temperature)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = settings.temperature.toFloat(),
                    onValueChange = { settingsRepository.updateTemperature(it.toDouble()) },
                    valueRange = 0f..2f,
                    steps = 19
                )
                Text(
                    text = "Более высокие значения делают ответы более случайными",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Divider()

                // Max Tokens
                Text(
                    text = "Максимум токенов",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = settings.maxTokens?.toString() ?: "",
                    onValueChange = { value ->
                        val tokens = value.toIntOrNull()
                        settingsRepository.updateMaxTokens(tokens)
                    },
                    placeholder = { Text("По умолчанию (без ограничений)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Оставьте пустым для значения по умолчанию",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Divider()

                // Кнопка сброса
                Button(
                    onClick = { settingsRepository.resetToDefaults() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить настройки")
                }
            }
        }
    }
}