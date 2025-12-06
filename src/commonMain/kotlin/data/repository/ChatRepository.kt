package data.repository

import data.network.LLMApi
import data.network.model.ChatMessage
import data.network.model.MessageRole
import domain.ApiError
import domain.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Интерфейс репозитория для работы с чатом
 */
interface ChatRepository {
    /**
     * Поток сообщений чата
     */
    suspend fun messagesFlow(): StateFlow<List<Message>>

    /**
     * Отправляет system prompt с приветственным сообщением и учитывает историю
     */
    suspend fun sendSystemPromptWithHistory(prompt: String)

    /**
     * Отправляет сообщение пользователя с учетом истории
     */
    suspend fun sendUserMessageWithHistory(userMessageText: String)

    /**
     * Очищает все сообщения
     */
    fun clearMessages()

    /**
     * Добавляет сообщение пользователя в список сообщений (без отправки в API)
     */
    fun addUserMessage(content: String)

    /**
     * Добавляет сообщение ассистента в список сообщений (без отправки в API)
     */
    fun addAssistantMessage(content: String)

    /**
     * Отправляет сообщение с заданной историей диалога
     * Возвращает сырой ответ от API для дальнейшей обработки
     */
    suspend fun sendMessageWithHistory(history: List<ChatMessage>): Result<data.network.model.ChatResponse>
}

/**
 * Реализация репозитория для работы с чатом
 */
class ChatRepositoryImpl(
    private val llmApiClient: LLMApi,
    private val settingsRepository: SettingsRepository
) : ChatRepository {
    // In-memory cache для сообщений
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    override suspend fun messagesFlow(): StateFlow<List<Message>> = _messages.asStateFlow()

    companion object {
        // Максимальное количество сообщений в истории для отправки в API
        // Это предотвращает превышение лимита токенов и уменьшает потребление памяти
        private const val MAX_HISTORY_MESSAGES = 20
    }

    /**
     * Отправляет system prompt с приветственным сообщением и учитывает историю
     */
    override suspend fun sendSystemPromptWithHistory(prompt: String) {
        // Конвертируем текущие сообщения в ChatMessage
        val history = _messages.value
            .takeLast(MAX_HISTORY_MESSAGES)
            .map { message ->
                ChatMessage(
                    role = MessageRole.valueOf(value = message.role.uppercase()),
                    content = message.content
                )
            }

        // Создаем список сообщений: system prompt + история + приветственное сообщение
        val messages = buildList {
            add(
                ChatMessage(
                    role = MessageRole.SYSTEM,
                    content = prompt
                )
            )
            addAll(history)
            if (history.isEmpty()) {
                add(
                    ChatMessage(
                        role = MessageRole.USER,
                        content = "Поздоровайся и опиши очень кратко чем ты можешь быть полезен"
                    )
                )
            }
        }

        sendMessage(messages = messages)
    }

    /**
     * Отправляет сообщение пользователя с учетом истории
     */
    override suspend fun sendUserMessageWithHistory(userMessageText: String) {
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

    override fun clearMessages() {
        _messages.value = emptyList()
    }

    /**
     * Добавляет сообщение пользователя в список сообщений (без отправки в API)
     */
    override fun addUserMessage(content: String) {
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
    override fun addAssistantMessage(content: String) {
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
    override suspend fun sendMessageWithHistory(history: List<ChatMessage>): Result<data.network.model.ChatResponse> {
        val settings = settingsRepository.getCurrentSettings()
        return llmApiClient.sendMessage(
            messages = history,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens
        )
    }
}