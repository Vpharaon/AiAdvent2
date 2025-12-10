package data.repository

import data.mapper.MessageMapper
import data.network.model.ChatMessage
import data.network.model.MessageRole
import data.source.remote.LLMRemoteDataSource
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
    val messages: StateFlow<List<Message>>

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
}

/**
 * Реализация репозитория для работы с чатом.
 * Использует DataSource для работы с удаленным API и Mapper для конвертации моделей.
 */
class ChatRepositoryImpl(
    private val remoteDataSource: LLMRemoteDataSource,
    private val settingsRepository: SettingsRepository
) : ChatRepository {
    // In-memory cache для сообщений
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    override val messages: StateFlow<List<Message>> = _messages.asStateFlow()

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
     * Использует DataSource вместо прямого вызова API.
     */
    private suspend fun sendMessage(messages: List<ChatMessage>) {
        val settings = settingsRepository.getCurrentSettings()
        val selectedModel = settings.selectedLlmModel
        val result = remoteDataSource.sendMessages(
            messages = messages,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens,
            apiUrl = selectedModel.apiUrl,
            modelName = selectedModel.modelName
        )

        result.onSuccess { chatResponse ->

            val message = chatResponse.choices?.firstOrNull()?.message?.let {
                // API возвращает timestamp в секундах, конвертируем в миллисекунды
                val timestampMillis = chatResponse.created?.let { seconds ->
                    if (seconds < 10_000_000_000L) seconds * 1000 else seconds
                } ?: System.currentTimeMillis()

                Message(
                    id = chatResponse.id.orEmpty(),
                    content = it.content.orEmpty(),
                    role = MessageRole.ASSISTANT.value,
                    timestamp = timestampMillis,
                    promptTokens = chatResponse.tokenUsage?.promptTokens,
                    completionTokens = chatResponse.tokenUsage?.completionTokens,
                    totalTokens = chatResponse.tokenUsage?.totalTokens
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
}