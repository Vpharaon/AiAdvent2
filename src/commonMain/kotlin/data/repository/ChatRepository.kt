package data.repository

import data.network.LLMApi
import data.network.model.ChatMessage
import data.network.model.MessageRole
import domain.ApiError
import domain.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(
    private val llmApiClient: LLMApi,
    private val settingsRepository: SettingsRepository
) {
    // In-memory cache для сообщений
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    companion object {
        // Максимальное количество сообщений в истории для отправки в API
        // Это предотвращает превышение лимита токенов и уменьшает потребление памяти
        private const val MAX_HISTORY_MESSAGES = 20
    }

    suspend fun sendWelcomeMessage() {
        val systemMessage = ChatMessage(
            role = MessageRole.SYSTEM,
            content = "Ты — полезный AI-ассистент"
        )
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = "Поздоровайся и опиши очень кратко чем ты можешь быть полезен"
        )
        sendMessage(messages = listOf(systemMessage, userMessage))
    }

    fun getMessages(): List<Message> {
        return _messages.value
    }

    suspend fun sendUserText(userMessageText: String) {
        // Создаем сообщение пользователя для UI
        val userDomainMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = userMessageText,
            role = MessageRole.USER.value,
            timestamp = System.currentTimeMillis()
        )

        // Добавляем сообщение пользователя в список сообщений
        _messages.value += userDomainMessage

        // Берем только последние MAX_HISTORY_MESSAGES сообщений для отправки в API
        // Это предотвращает превышение лимита токенов
        // Примечание: UI продолжает показывать все сообщения
        val messages = _messages.value
            .takeLast(MAX_HISTORY_MESSAGES)
            .map { message ->
                ChatMessage(
                    role = MessageRole.valueOf(value = message.role.uppercase()),
                    content = message.content
                )
            }
        sendMessage(messages = messages)
    }

    /**
     * Отправляет сообщение пользователя, получает ответ от LLM и обновляет кеш сообщений.
     */
    private suspend fun sendMessage(messages: List<ChatMessage>) {
        val settings = settingsRepository.getCurrentSettings()
        val result: Result<data.network.model.ChatResponse> = llmApiClient.sendMessage(
            messages = messages,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens
        )

        result.onSuccess { chatResponse ->

            val message = chatResponse.choices?.firstOrNull()?.message?.let {
                Message(
                    id = chatResponse.id.orEmpty(),
                    content = it.content.orEmpty(),
                    role = MessageRole.ASSISTANT.value,
                    timestamp = chatResponse.created ?: System.currentTimeMillis()
                )
            }

            message?.let {
                _messages.value += it
            }

        }.onFailure { error ->
            // Если произошла ошибка (сеть, API и т.д.)
            val userFriendlyMessage = when (error) {
                is ApiError -> error.getUserFriendlyMessage()
                else -> "Ошибка: ${error.message ?: "Не удалось получить ответ от сервера"}"
            }

            val errorMessage = Message(
                id = System.currentTimeMillis().toString(),
                content = userFriendlyMessage,
                role = MessageRole.ASSISTANT.value,
                timestamp = System.currentTimeMillis()
            )
            _messages.value += errorMessage
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    /**
     * Добавляет сообщение пользователя в список сообщений (без отправки в API)
     */
    fun addUserMessage(content: String) {
        val userMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = content,
            role = MessageRole.USER.value,
            timestamp = System.currentTimeMillis()
        )
        _messages.value += userMessage
    }

    /**
     * Добавляет сообщение ассистента в список сообщений (без отправки в API)
     */
    fun addAssistantMessage(content: String) {
        val assistantMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = content,
            role = MessageRole.ASSISTANT.value,
            timestamp = System.currentTimeMillis()
        )
        _messages.value += assistantMessage
    }

    /**
     * Отправляет сообщение с заданной историей диалога
     * Возвращает сырой ответ от API для дальнейшей обработки
     */
    suspend fun sendMessageWithHistory(history: List<ChatMessage>): Result<data.network.model.ChatResponse> {
        val settings = settingsRepository.getCurrentSettings()
        return llmApiClient.sendMessage(
            messages = history,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens
        )
    }
}