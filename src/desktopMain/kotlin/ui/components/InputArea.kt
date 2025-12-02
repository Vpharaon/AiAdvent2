package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp

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