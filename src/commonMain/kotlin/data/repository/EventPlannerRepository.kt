package data.repository

import data.network.model.ChatMessage
import data.source.remote.LLMRemoteDataSource
import domain.Message
import domain.structured.EventPlanWithRaw
import domain.util.StructuredPromptBuilder
import domain.util.StructuredResponseParser
import data.network.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Интерфейс репозитория для работы с планировщиком мероприятий
 */
interface EventPlannerRepository {
    /**
     * Поток сообщений диалога
     */
    val messages: StateFlow<List<Message>>

    /**
     * Отправляет сообщение пользователя в диалог
     *
     * @param userMessage Текст сообщения пользователя
     * @return Result с опциональным распарсенным планом мероприятия
     */
    suspend fun sendMessage(userMessage: String): Result<EventPlanWithRaw?>

    /**
     * Инициализирует диалог с системным промптом и приветственным сообщением
     *
     * @return Result с первым сообщением ассистента
     */
    suspend fun startConversation(): Result<Message>

    /**
     * Очищает историю диалога
     */
    fun clearConversation()
}

/**
 * Реализация репозитория для работы с планировщиком мероприятий.
 * Управляет диалогом с LLM для сбора информации и создания плана мероприятия.
 */
class EventPlannerRepositoryImpl(
    private val remoteDataSource: LLMRemoteDataSource,
    private val settingsRepository: SettingsRepository
) : EventPlannerRepository {

    private val parser = StructuredResponseParser()
    private val promptBuilder = StructuredPromptBuilder()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // История диалога для API
    private val conversationHistory = mutableListOf<ChatMessage>()

    // Сообщения для UI
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    override val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    override suspend fun sendMessage(userMessage: String): Result<EventPlanWithRaw?> {
        // Добавляем сообщение пользователя в UI
        val userDomainMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = userMessage,
            role = MessageRole.USER.value,
            timestamp = System.currentTimeMillis()
        )
        _messages.value += userDomainMessage

        // Добавляем в историю для API
        conversationHistory.add(
            ChatMessage(
                role = MessageRole.USER,
                content = userMessage
            )
        )

        // Отправляем запрос
        val settings = settingsRepository.getCurrentSettings()
        val result = remoteDataSource.sendMessages(
            messages = conversationHistory,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens
        )

        return result.fold(
            onSuccess = { response ->
                val assistantContent = response.choices?.firstOrNull()?.message?.content

                if (assistantContent != null) {
                    // Добавляем ответ в историю
                    conversationHistory.add(
                        ChatMessage(
                            role = MessageRole.ASSISTANT,
                            content = assistantContent
                        )
                    )

                    // Добавляем ответ в UI
                    // API возвращает timestamp в секундах, конвертируем в миллисекунды
                    val timestampMillis = response.created?.let { seconds ->
                        if (seconds < 10_000_000_000L) seconds * 1000 else seconds
                    } ?: System.currentTimeMillis()

                    val assistantDomainMessage = Message(
                        id = response.id.orEmpty(),
                        content = assistantContent,
                        role = MessageRole.ASSISTANT.value,
                        timestamp = timestampMillis
                    )
                    _messages.value += assistantDomainMessage

                    // Пробуем распарсить план
                    val parseResult = parser.parseEventPlanWithRaw(assistantContent)
                    val eventPlanData = if (parseResult.isSuccess) {
                        val plan = parseResult.getOrNull()!!
                        val fullResponseJson = json.encodeToString(response)
                        plan.copy(fullResponseJson = fullResponseJson)
                    } else {
                        null
                    }

                    Result.success(eventPlanData)
                } else {
                    Result.failure(IllegalStateException("Получен пустой ответ от сервера"))
                }
            },
            onFailure = { error ->
                // Откатываем последнее сообщение пользователя из истории
                if (conversationHistory.isNotEmpty()) {
                    conversationHistory.removeAt(conversationHistory.size - 1)
                }
                Result.failure(error)
            }
        )
    }

    override suspend fun startConversation(): Result<Message> {
        // Очищаем перед стартом
        conversationHistory.clear()
        _messages.value = emptyList()

        // Добавляем системный промпт
        val systemPrompt = promptBuilder.buildEventPlannerPrompt()
        conversationHistory.add(
            ChatMessage(
                role = MessageRole.SYSTEM,
                content = systemPrompt
            )
        )

        // Добавляем начальное сообщение пользователя
        val initialUserMessage = "Hello! I would like to organize a New Year's corporate party for our company."
        conversationHistory.add(
            ChatMessage(
                role = MessageRole.USER,
                content = initialUserMessage
            )
        )

        // Получаем первое сообщение от менеджера
        val settings = settingsRepository.getCurrentSettings()
        val result = remoteDataSource.sendMessages(
            messages = conversationHistory,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens
        )

        return result.fold(
            onSuccess = { response ->
                val assistantContent = response.choices?.firstOrNull()?.message?.content

                if (assistantContent != null) {
                    conversationHistory.add(
                        ChatMessage(
                            role = MessageRole.ASSISTANT,
                            content = assistantContent
                        )
                    )

                    // Добавляем ответ в UI
                    // API возвращает timestamp в секундах, конвертируем в миллисекунды
                    val timestampMillis = response.created?.let { seconds ->
                        if (seconds < 10_000_000_000L) seconds * 1000 else seconds
                    } ?: System.currentTimeMillis()

                    val assistantDomainMessage = Message(
                        id = response.id.orEmpty(),
                        content = assistantContent,
                        role = MessageRole.ASSISTANT.value,
                        timestamp = timestampMillis
                    )
                    _messages.value = listOf(assistantDomainMessage)

                    Result.success(assistantDomainMessage)
                } else {
                    Result.failure(IllegalStateException("Не удалось получить приветствие от менеджера"))
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override fun clearConversation() {
        conversationHistory.clear()
        _messages.value = emptyList()
    }
}