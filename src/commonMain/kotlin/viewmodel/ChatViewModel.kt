package viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Message
import repository.ChatRepository

class ChatViewModel(
    private val repository: ChatRepository,
    private val coroutineScope: CoroutineScope
) {
    // Состояние списка сообщений для UI
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Состояние ввода для поля ввода
    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    // Состояние "бот печатает"
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        // Подписываемся на изменения в репозитории
        coroutineScope.launch {
            repository.messages.collect { newMessages ->
                _messages.value = newMessages
            }
        }
    }

    // Обновление текста ввода
    fun updateInput(text: String) {
        _input.value = text
    }

    // Метод sendMessage - отправляет сообщение через репозиторий
    fun sendMessage() {
        val messageText = _input.value.trim()
        if (messageText.isEmpty()) return

        // Очищаем поле ввода
        _input.value = ""

        // Отправляем сообщение в фоновом потоке
        coroutineScope.launch {
            _isTyping.value = true
            repository.sendMessage(messageText)
            _isTyping.value = false
        }
    }

    // Метод clearChat - очищает кеш и UI
    fun clearChat() {
        repository.clearMessages()
        _input.value = ""
    }
}