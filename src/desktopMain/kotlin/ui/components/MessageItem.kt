package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.Message
import ui.MarkdownText
import ui.util.TimeFormatter

/**
 * Компонент отображения одного сообщения в чате.
 *
 * Отображает сообщение в виде карточки с различным оформлением в зависимости от роли отправителя:
 * - Сообщения пользователя: справа, с primary цветом
 * - Сообщения ассистента: слева, с surfaceVariant цветом
 *
 * Функции:
 * - Поддержка Markdown форматирования в тексте сообщения
 * - Возможность выделения и копирования текста
 * - Отображение статистики токенов для ответов ассистента
 * - Отображение времени отправки сообщения
 *
 * Статистика токенов включает:
 * - Общее количество токенов
 * - Количество токенов промпта (запроса)
 * - Количество токенов ответа
 *
 * @param message Модель сообщения для отображения
 */
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
            modifier = Modifier.widthIn(max = 400.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            SelectionContainer {
                Column(modifier = Modifier.padding(12.dp)) {
                    MarkdownText(
                        markdown = message.content,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Информация о токенах (только для сообщений ассистента)
                    if (message.isAssistant && message.totalTokens != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Токены: ${message.totalTokens}",
                                color = textColor.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            message.promptTokens?.let { prompt ->
                                Text(
                                    text = "●",
                                    color = textColor.copy(alpha = 0.3f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Промпт: $prompt",
                                    color = textColor.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            message.completionTokens?.let { completion ->
                                Text(
                                    text = "●",
                                    color = textColor.copy(alpha = 0.3f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Ответ: $completion",
                                    color = textColor.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Время сообщения
                    Text(
                        text = TimeFormatter.formatTime(message.timestamp),
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}