package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ui.components.InputArea
import ui.components.MessagesArea
import viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    onOpenSettings: () -> Unit,
    onOpenRecipes: () -> Unit
) {
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
        // Заголовок с кнопками
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onOpenRecipes) {
                    Text("👨‍🍳", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onOpenSettings) {
                    Text("⚙️", style = MaterialTheme.typography.titleLarge)
                }
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