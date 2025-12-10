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
import ui.components.AgentSidebar
import ui.components.InputArea
import ui.components.MessagesArea
import ui.components.ModelSidebar

@Composable
fun ChatScreen(component: component.ChatComponent) {
    val state by component.state.collectAsState()
    val messages = state.messages
    val input = state.input
    val isTyping = state.isTyping
    val availableModels = state.availableModels
    val selectedModel = state.selectedModel
    val availableAgents = state.availableAgents
    val selectedAgent = state.selectedAgent
    val inputTokenCount = state.inputTokenCount

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
                text = "AI Ассистент",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = component::onSettingsClick) {
                Text("⚙️", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Основной контент с разделением на 3 панели
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Левая панель - список моделей (вертикальный) - всегда открыта
            ModelSidebar(
                models = availableModels,
                selectedModel = selectedModel,
                onModelSelect = component::onSelectLlmModel,
                modifier = Modifier.width(280.dp).fillMaxHeight()
            )

            // Центральная часть - чат
            Column(
                modifier = Modifier.weight(1f)
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
                    onInputChange = component::onInputChange,
                    onSendClick = component::onSendClick,
                    onClearClick = component::onClearClick,
                    tokenCount = inputTokenCount
                )
            }

            // Правая панель - выбор агента (всегда открыта)
            AgentSidebar(
                agents = availableAgents,
                selectedAgent = selectedAgent,
                onAgentSelect = component::onSelectAgent,
                modifier = Modifier.width(320.dp).fillMaxHeight()
            )
        }
    }
}