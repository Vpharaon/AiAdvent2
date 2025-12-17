package data.repository

import data.network.model.ChatMessage
import data.network.model.McpToolConverter
import data.network.model.MessageRole
import data.source.local.ChatLocalDataSource
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
     *
     * @param prompt Системный промпт
     * @param currentAgent Текущий агент (опционально, для поддержки tools)
     */
    suspend fun sendSystemPromptWithHistory(prompt: String, currentAgent: domain.Agent? = null)

    /**
     * Отправляет сообщение пользователя с учетом истории
     *
     * @param userMessageText Текст сообщения пользователя
     * @param currentAgent Текущий агент (опционально, для поддержки tools)
     */

    suspend fun sendUserMessageWithHistory(userMessageText: String, currentAgent: domain.Agent? = null)

    /**
     * Очищает все сообщения
     */
    suspend fun clearMessages()

    /**
     * Создает сжатую версию истории диалога
     */
    suspend fun summarizeHistory()

    /**
     * Загружает историю чата из локального хранилища
     */
    suspend fun loadHistory()
}

/**
 * Реализация репозитория для работы с чатом.
 * Использует DataSource для работы с удаленным API и Mapper для конвертации моделей.
 */
class ChatRepositoryImpl(
    private val remoteDataSource: LLMRemoteDataSource,
    private val settingsRepository: SettingsRepository,
    private val localDataSource: ChatLocalDataSource? = null,
    private val toolCallHandler: ToolCallHandler
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
    override suspend fun sendSystemPromptWithHistory(prompt: String, currentAgent: domain.Agent?) {
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

        sendMessage(messages = messages, currentAgent = currentAgent)
    }

    /**
     * Отправляет сообщение пользователя с учетом истории
     */
    override suspend fun sendUserMessageWithHistory(userMessageText: String, currentAgent: domain.Agent?) {
        // Создаем сообщение пользователя для UI
        val userDomainMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = userMessageText,
            role = MessageRole.USER.value,
            timestamp = System.currentTimeMillis()
        )

        // Добавляем сообщение пользователя в список сообщений
        _messages.value += userDomainMessage

        // Сохраняем историю с сообщением пользователя
        localDataSource?.saveChatHistory(_messages.value)

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

        sendMessage(messages = messages, currentAgent = currentAgent)
    }

    /**
     * Отправляет сообщение пользователя, получает ответ от LLM и обновляет кеш сообщений.
     * Использует DataSource вместо прямого вызова API.
     * Поддерживает tool calls для агентов с hasTools = true.
     */
    private suspend fun sendMessage(
        messages: List<ChatMessage>,
        currentAgent: domain.Agent? = null
    ) {
        val settings = settingsRepository.getCurrentSettings()
        val selectedModel = settings.selectedLlmModel

        // Определяем нужны ли tools для текущего агента
        val tools = McpToolConverter.createWeatherTools() +
                    McpToolConverter.createTimeTools() +
                    McpToolConverter.createReminderTools()

        val result = remoteDataSource.sendMessages(
            messages = messages,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens,
            apiUrl = selectedModel.apiUrl,
            modelName = selectedModel.modelName,
            tools = tools
        )

        result.onSuccess { chatResponse ->
            val assistantMessage = chatResponse.choices?.firstOrNull()?.message

            // Проверяем есть ли tool calls в ответе
            if (assistantMessage?.toolCalls != null && assistantMessage.toolCalls.isNotEmpty()) {
                // LLM хочет вызвать функцию
                // 1. Сохраняем сообщение ассистента с tool calls (не показываем пользователю)
                // 2. Выполняем tool calls
                val toolResultMessages = toolCallHandler.handleToolCalls(assistantMessage.toolCalls)

                // 3. Отправляем новый запрос с результатами tool calls
                val newMessages = messages + listOf(
                    ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = assistantMessage.content,
                        toolCalls = assistantMessage.toolCalls
                    )
                ) + toolResultMessages

                // Рекурсивно вызываем sendMessage с результатами
                sendMessage(newMessages, currentAgent)
            } else {
                // Обычный ответ без tool calls
                val message = assistantMessage?.let {
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
                    localDataSource?.saveChatHistory(_messages.value)
                }
            }

        }.onFailure { error ->
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

    override suspend fun clearMessages() {
        _messages.value = emptyList()
        // Очищаем содержимое файла с историей (записываем пустой массив)
        localDataSource?.clearChatHistory()
    }

    /**
     * Загружает историю чата из локального хранилища
     */
    override suspend fun loadHistory() {
        val history = localDataSource?.loadChatHistory() ?: emptyList()
        if (history.isNotEmpty()) {
            _messages.value = history
        }
    }

    /**
     * Создает сжатую версию истории диалога
     */
    override suspend fun summarizeHistory() {
        // Получаем текущие сообщения
        val currentMessages = _messages.value

        if (currentMessages.isEmpty()) {
            return
        }

        // Формируем историю для сжатия
        val historyText = currentMessages.joinToString("\n\n") { message ->
            val roleLabel = when {
                message.isUser -> MessageRole.USER.value
                message.isAssistant -> MessageRole.ASSISTANT.value
                message.isSystem -> MessageRole.SYSTEM.value
                else -> message.role
            }
            "$roleLabel: ${message.content}"
        }

        // Создаем промпт для сжатия
        val summarizePrompt = """
            Выполни сжатие следующей истории диалога. Создай краткую сводку, которая сохранит все ключевые моменты,
            важные детали и контекст разговора. Сводка должна быть достаточно подробной, чтобы можно было продолжить
            общение на основе этой информации.

            История диалога:
            $historyText

            Создай структурированную сводку диалога.
        """.trimIndent()

        // Создаем сообщение для отправки
        val messages = listOf(
            ChatMessage(
                role = MessageRole.USER,
                content = summarizePrompt
            )
        )

        // Отправляем запрос
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
            val summaryContent = chatResponse.choices?.firstOrNull()?.message?.content

            if (summaryContent != null) {
                // Очищаем всю историю
                _messages.value = emptyList()

                // Добавляем только сводку как единственное сообщение
                val timestampMillis = chatResponse.created?.let { seconds ->
                    if (seconds < 10_000_000_000L) seconds * 1000 else seconds
                } ?: System.currentTimeMillis()

                val summaryMessage = Message(
                    id = chatResponse.id.orEmpty(),
                    content = summaryContent,
                    role = MessageRole.ASSISTANT.value,
                    timestamp = timestampMillis,
                    promptTokens = chatResponse.tokenUsage?.promptTokens,
                    completionTokens = chatResponse.tokenUsage?.completionTokens,
                    totalTokens = chatResponse.tokenUsage?.totalTokens
                )

                _messages.value = listOf(summaryMessage)

                // Сохраняем сводку в файл
                localDataSource?.saveChatHistory(listOf(summaryMessage))
            }
        }.onFailure { error ->
            // Если произошла ошибка, выводим сообщение об ошибке
            val userFriendlyMessage = when (error) {
                is ApiError -> error.getUserFriendlyMessage()
                else -> "Ошибка при создании сводки: ${error.message ?: "Не удалось получить ответ от сервера"}"
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
}