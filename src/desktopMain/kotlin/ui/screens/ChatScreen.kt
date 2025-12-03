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
import ui.components.InputArea
import ui.components.MessagesArea

@Composable
fun ChatScreen(component: component.ChatComponent) {
    val state by component.state.collectAsState()
    val messages = state.messages
    val input = state.input
    val isTyping = state.isTyping

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = component::onBackClick) {
                    Text("←", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = "AI Чат",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = component::onSettingsClick) {
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
            onInputChange = component::onInputChange,
            onSendClick = component::onSendClick,
            onClearClick = component::onClearClick
        )
    }
}