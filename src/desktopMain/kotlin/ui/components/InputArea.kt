package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp

/**
 * Область ввода сообщения с кнопками управления и отображением статистики токенов.
 *
 * Компонент предоставляет текстовое поле для ввода сообщений пользователем,
 * кнопки отправки, очистки чата и создания сводки, а также отображает статистику использования токенов.
 *
 * Особенности:
 * - Поддержка многострочного ввода (до 4 строк)
 * - Отправка сообщения по нажатию Enter (Shift+Enter для новой строки)
 * - Отображение количества токенов в текущем вводе
 * - Отображение общей статистики токенов по всем сообщениям
 *
 * @param input Текст текущего ввода пользователя
 * @param onInputChange Callback, вызываемый при изменении текста ввода
 * @param onSendClick Callback, вызываемый при нажатии кнопки "Отправить"
 * @param onClearClick Callback, вызываемый при нажатии кнопки "Очистить чат"
 * @param onSummaryClick Callback, вызываемый при нажатии кнопки "Summary"
 * @param tokenCount Количество токенов в текущем вводе (null если не посчитано)
 * @param totalPromptTokens Общее количество токенов во всех промптах (запросах)
 * @param totalCompletionTokens Общее количество токенов во всех ответах
 * @param totalTokens Общее количество токенов (промпты + ответы)
 */
@Composable
fun InputArea(
    input: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onClearClick: () -> Unit,
    onSummaryClick: () -> Unit,
    tokenCount: Int? = null,
    totalPromptTokens: Int = 0,
    totalCompletionTokens: Int = 0,
    totalTokens: Int = 0
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Отображение статистики токенов
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Токены текущего ввода
            if (tokenCount != null && tokenCount > 0) {
                Text(
                    text = "Токенов в вводе: $tokenCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Общая статистика токенов
            if (totalTokens > 0) {
                Text(
                    text = "Всего токенов: $totalTokens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Запросы: $totalPromptTokens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ответы: $totalCompletionTokens",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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

        // Кнопка Отправить
        Button(
            onClick = onSendClick,
            modifier = Modifier.height(56.dp),
            enabled = input.trim().isNotEmpty()
        ) {
            Text("Отправить")
        }

        // Кнопка Summary
        OutlinedButton(
            onClick = onSummaryClick,
            modifier = Modifier.height(56.dp)
        ) {
            Text("Сжатие")
        }

        // Кнопка Очистить чат
        OutlinedButton(
            onClick = onClearClick,
            modifier = Modifier.height(56.dp)
        ) {
            Text("Очистить чат")
        }
        }
    }
}