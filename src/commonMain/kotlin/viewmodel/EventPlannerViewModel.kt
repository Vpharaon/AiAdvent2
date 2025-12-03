package viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import domain.Message
import data.repository.ChatRepository
import data.parser.StructuredResponseParser
import domain.structured.EventPlanWithRaw
import data.prompt.StructuredPromptBuilder
import data.network.model.ChatMessage
import data.network.model.MessageRole
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EventPlannerViewModel(
    private val repository: ChatRepository,
    private val coroutineScope: CoroutineScope
) {
    private val parser = StructuredResponseParser()
    private val promptBuilder = StructuredPromptBuilder()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Состояние списка сообщений для UI
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Состояние ввода для поля ввода
    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    // Состояние "бот печатает"
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    // Состояние финального плана мероприятия
    private val _eventPlan = MutableStateFlow<EventPlanWithRaw?>(null)
    val eventPlan: StateFlow<EventPlanWithRaw?> = _eventPlan.asStateFlow()

    // Состояние ошибки
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // История сообщений для API (для контекста диалога)
    private val conversationHistory = mutableListOf<ChatMessage>()

    init {
        // Подписываемся на изменения в репозитории
        coroutineScope.launch {
            repository.messages.collect { newMessages ->
                _messages.value = newMessages
            }
        }

        // Инициализируем диалог с системным промптом
        coroutineScope.launch {
            startConversation()
        }
    }

    private suspend fun startConversation() {
        _isTyping.value = true

        try {
            // Добавляем системный промпт
            val systemPrompt = promptBuilder.buildEventPlannerPrompt()
            conversationHistory.add(
                ChatMessage(
                    role = MessageRole.SYSTEM,
                    content = systemPrompt
                )
            )

            // Добавляем начальное сообщение пользователя для инициации диалога
            val initialUserMessage = "Здравствуйте! Я хочу организовать новогодний корпоратив для нашей компании."
            conversationHistory.add(
                ChatMessage(
                    role = MessageRole.USER,
                    content = initialUserMessage
                )
            )

            // Получаем первое сообщение от менеджера (приветствие и представление)
            val result = repository.sendMessageWithHistory(conversationHistory)

            result.onSuccess { response ->
                val assistantMessage = response.choices?.firstOrNull()?.message?.content

                if (assistantMessage != null) {
                    // Добавляем ответ ассистента в историю
                    conversationHistory.add(
                        ChatMessage(
                            role = MessageRole.ASSISTANT,
                            content = assistantMessage
                        )
                    )

                    // Добавляем сообщение в UI
                    repository.addAssistantMessage(assistantMessage)
                } else {
                    _errorMessage.value = "Не удалось получить приветствие от менеджера"
                }
            }.onFailure { error ->
                _errorMessage.value = "Ошибка инициализации: ${error.message}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка: ${e.message}"
        } finally {
            _isTyping.value = false
        }
    }

    // Обновление текста ввода
    fun updateInput(text: String) {
        if (text.length <= MAX_MESSAGE_LENGTH) {
            _input.value = text
        }
    }

    // Отправка сообщения пользователя
    fun sendMessage() {
        val messageText = _input.value.trim()
        if (messageText.isEmpty()) return

        // Очищаем поле ввода
        _input.value = ""

        coroutineScope.launch {
            _isTyping.value = true
            _errorMessage.value = null

            try {
                // Добавляем сообщение пользователя в историю
                conversationHistory.add(
                    ChatMessage(
                        role = MessageRole.USER,
                        content = messageText
                    )
                )

                // Отправляем через репозиторий (для отображения в UI)
                repository.addUserMessage(messageText)

                // Получаем ответ от LLM через ChatRepository
                val result = repository.sendMessageWithHistory(conversationHistory)

                result.onSuccess { response ->
                    val assistantMessage = response.choices?.firstOrNull()?.message?.content

                    if (assistantMessage != null) {
                        // Добавляем ответ ассистента в историю
                        conversationHistory.add(
                            ChatMessage(
                                role = MessageRole.ASSISTANT,
                                content = assistantMessage
                            )
                        )

                        // Пытаемся распарсить JSON, если он есть
                        val parseResult = parser.parseEventPlanWithRaw(assistantMessage)

                        if (parseResult.isSuccess) {
                            // Успешно распарсили - это финальный ответ с планом
                            val eventPlanData = parseResult.getOrNull()!!

                            // Сериализуем полный ответ от API
                            val fullResponseJson = json.encodeToString(response)

                            _eventPlan.value = eventPlanData.copy(fullResponseJson = fullResponseJson)

                            // Добавляем сообщение в UI
                            repository.addAssistantMessage(assistantMessage)
                        } else {
                            // Это обычное сообщение (вопрос от менеджера)
                            repository.addAssistantMessage(assistantMessage)
                        }
                    } else {
                        _errorMessage.value = "Получен пустой ответ от сервера"
                    }
                }.onFailure { error ->
                    _errorMessage.value = "Ошибка: ${error.message}"
                    // Удаляем последнее сообщение пользователя из истории при ошибке
                    if (conversationHistory.isNotEmpty()) {
                        conversationHistory.removeAt(conversationHistory.size - 1)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isTyping.value = false
            }
        }
    }

    // Очистка чата и перезапуск диалога
    fun clearChat() {
        repository.clearMessages()
        conversationHistory.clear()
        _input.value = ""
        _eventPlan.value = null
        _errorMessage.value = null

        coroutineScope.launch {
            startConversation()
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}