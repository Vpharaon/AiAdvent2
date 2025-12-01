import androidx.compose.foundation.background
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
import di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import model.Message
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import viewmodel.ChatViewModel

fun main() = application {
    // GLM API ключ (можно вынести в переменные окружения или конфиг файл)
    val apiKey = System.getenv("GLM_API_KEY") ?: "your-glm-api-key-here"

    // Корутин Scope для приложения
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
            MaterialTheme {
                ChatScreen()
            }
        }
    }
}

@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = koinInject()
    val messages by viewModel.messages.collectAsState()
    val input by viewModel.input.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
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
                containerColor = Color(0xFFE8E8E8)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bot is typing",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) Color(0xFF007AFF) else Color(0xFFE8E8E8)
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
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
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
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