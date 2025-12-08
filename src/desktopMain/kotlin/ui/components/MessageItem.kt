package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import domain.Message
import kotlinx.coroutines.delay
import ui.MarkdownText
import ui.util.TimeFormatter
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageItem(message: Message) {
    var showCopiedMessage by remember { mutableStateOf(false) }

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
        Box {
            Card(
                modifier = Modifier
                    .widthIn(max = 400.dp),
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

                    // Строка с временем и кнопкой копирования
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = TimeFormatter.formatTime(message.timestamp),
                            color = textColor.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Кнопка копирования
                        Surface(
                            onClick = {
                                val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                                val stringSelection = java.awt.datatransfer.StringSelection(message.content)
                                clipboard.setContents(stringSelection, null)
                                showCopiedMessage = true
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
                            color = textColor.copy(alpha = 0.1f),
                            contentColor = textColor,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📋",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Индикатор "Скопировано!"
            if (showCopiedMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = (-32).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Скопировано",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Скрываем индикатор через 2 секунды
            LaunchedEffect(showCopiedMessage) {
                if (showCopiedMessage) {
                    delay(2000)
                    showCopiedMessage = false
                }
            }
        }
    }
}