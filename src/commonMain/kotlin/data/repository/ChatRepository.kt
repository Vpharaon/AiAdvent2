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

        val messages = _messages.value.map { message ->
            ChatMessage(
                role = MessageRole.valueOf(value = message.role.uppercase()),
                content = message.content
            )
        }.toMutableList()
        sendMessage(messages = messages)
    }

    /**
     * Отправляет сообщение пользователя, получает ответ от LLM и обновляет кеш сообщений.
     */
    private suspend fun sendMessage(messages: List<ChatMessage>) {
        val result: Result<data.network.model.ChatResponse> = llmApiClient.sendMessage(messages = messages)

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

            //println()
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}