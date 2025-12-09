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
import ui.components.ModelSidebar

@Composable
fun ChatScreen(component: component.ChatComponent) {
    val state by component.state.collectAsState()
    val messages = state.messages
    val input = state.input
    val isTyping = state.isTyping
    val systemPrompt = state.systemPrompt
    val isEditorExpanded = state.isSystemPromptExpanded
    val availableModels = state.availableModels
    val selectedModel = state.selectedModel

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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = component::onToggleSystemPromptEditor) {
                    Text(
                        if (isEditorExpanded) "✏️" else "👤",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                IconButton(onClick = component::onSettingsClick) {
                    Text("⚙️", style = MaterialTheme.typography.titleLarge)
                }
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
                    onClearClick = component::onClearClick
                )
            }

            // Правая панель - редактор роли агента
            if (isEditorExpanded) {
                Card(
                    modifier = Modifier.width(400.dp).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Роль агента",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = component::onToggleSystemPromptEditor) {
                                Text("✕", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        OutlinedTextField(
                            value = systemPrompt,
                            onValueChange = component::onSystemPromptChange,
                            placeholder = { Text("Введите описание роли AI агента") },
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = component::onSaveSystemPromptClick,
                            enabled = systemPrompt.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сохранить и применить")
                        }
                    }
                }
            }
        }
    }
}