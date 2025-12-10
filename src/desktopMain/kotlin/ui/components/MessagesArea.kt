package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.Message

/**
 * Область отображения списка сообщений чата.
 *
 * Компонент отображает прокручиваемый список всех сообщений в чате с автоматической
 * прокруткой к последнему сообщению при добавлении новых сообщений или появлении
 * индикатора "печатает...".
 *
 * Особенности:
 * - Ленивая загрузка сообщений через LazyColumn для оптимизации производительности
 * - Автоматическая анимированная прокрутка к последнему сообщению
 * - Отображение индикатора "Агент печатает..." во время обработки запроса
 * - Уникальные ключи для каждого сообщения для оптимизации рекомпозиции
 *
 * @param messages Список всех сообщений для отображения
 * @param isTyping Флаг, указывающий, что ассистент в процессе генерации ответа
 * @param modifier Модификатор для кастомизации компонента
 */
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message = message)
            }

            // Индикатор "Агент печатает..."
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}