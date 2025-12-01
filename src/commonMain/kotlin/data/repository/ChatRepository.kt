package data.repository

import data.network.LLMApiClient
import data.network.model.ChatMessage
import data.network.model.MessageRole
import domain.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(
    private val llmApiClient: LLMApiClient
) {
    // In-memory cache для сообщений
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    suspend fun sendWelcomeMessage() {
        val message = ChatMessage(
            role = MessageRole.SYSTEM,
            content = "Ты — полезный AI-ассистент."
        )
        llmApiClient.sendMessage(messages = listOf(message))
    }

    fun getMessages(): List<Message> {
        return _messages.value
    }

    /**
     * Отправляет сообщение пользователя, получает ответ от LLM и обновляет кеш сообщений.
     */
    suspend fun sendMessage(userMessageText: String) {
        val chatMessagesForApi = _messages.value.map { message ->
            ChatMessage(
                role = MessageRole.valueOf(value = message.role),
                content = message.content
            )
        }.toMutableList()

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = userMessageText
        )
        chatMessagesForApi.add(userMessage)

        val result: Result<data.network.model.ChatResponse> = llmApiClient.sendMessage(chatMessagesForApi)

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
            /*// Если произошла ошибка (сеть, API и т.д.)
            val errorMessage = Message(
                id = generateId(),
                content = "Ошибка: ${error.message}",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + errorMessage*/
        }
    }

    suspend fun clearMessages() {
        _messages.value = emptyList()
        sendWelcomeMessage()
    }
}