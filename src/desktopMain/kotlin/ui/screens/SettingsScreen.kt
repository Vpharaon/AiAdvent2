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
import data.repository.SettingsRepository
import domain.Theme
import org.koin.compose.koinInject

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

                HorizontalDivider()

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

                HorizontalDivider()

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

                HorizontalDivider()

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