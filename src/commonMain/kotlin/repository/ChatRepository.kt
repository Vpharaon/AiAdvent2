package repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.LLMResponse
import model.Message
import kotlin.random.Random

class ChatRepository(
    private val llmApiClient: suspend (String) -> LLMResponse
) {
    // In-memory cache для сообщений
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        // Добавляем приветственное сообщение от LLM при запуске
        addWelcomeMessage()
    }

    // Простая генерация ID
    private fun generateId(): String {
        return "${System.currentTimeMillis()}-${Random.nextInt(10000, 99999)}"
    }

    // Добавление приветственного сообщения
    private fun addWelcomeMessage() {
        val welcomeMessage = Message(
            id = generateId(),
            content = "Привет! Я ваш AI-ассистент. Чем могу помочь?",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = listOf(welcomeMessage)
    }

    // Метод getMessages - возвращает текущий список сообщений
    fun getMessages(): List<Message> {
        return _messages.value
    }

    // Метод sendMessage - добавляет сообщение пользователя, отправляет на LLM API, добавляет ответ
    suspend fun sendMessage(userMessage: String) {
        // Добавляем сообщение пользователя в кеш
        val message = Message(
            id = generateId(),
            content = userMessage,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + message

        // Отправляем запрос на LLM API
        try {
            val response = llmApiClient(userMessage)

            if (response.success) {
                // Добавляем ответ LLM в кеш
                val botMessage = Message(
                    id = generateId(),
                    content = response.message,
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + botMessage
            } else {
                // Добавляем сообщение об ошибке
                val errorMessage = Message(
                    id = generateId(),
                    content = "Ошибка: ${response.message}",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + errorMessage
            }
        } catch (e: Exception) {
            // Обработка ошибок сети
            val errorMessage = Message(
                id = generateId(),
                content = "Ошибка сети: ${e.message}",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + errorMessage
        }
    }

    // Метод clearMessages - очищает кеш и начинает новый диалог
    fun clearMessages() {
        _messages.value = emptyList()
        addWelcomeMessage()
    }
}