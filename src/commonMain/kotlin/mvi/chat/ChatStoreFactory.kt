package mvi.chat

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.ChatRepository
import domain.service.getTokenCounter
import domain.usecase.chat.ClearChatUseCase
import domain.usecase.chat.SendMessageUseCase
import domain.usecase.chat.SendSystemPromptUseCase
import kotlinx.coroutines.launch
import mvi.chat.ChatStoreFactory.Message.*

/**
 * Factory для создания ChatStore.
 * Использует Use Cases для выполнения бизнес-логики.
 *
 * ## Управление жизненным циклом запросов к LLM
 *
 * Данная реализация обеспечивает корректную отмену активных запросов к LLM при:
 * - Очистке чата (ClearChat)
 * - Смене агента (SelectAgent)
 * - Отправке нового сообщения (SendMessage)
 *
 * ### Архитектура отмены запросов:
 *
 * 1. **Job tracking**: Каждый активный запрос к LLM отслеживается через `activeRequestJob`.
 *    Это позволяет в любой момент отменить запрос вызовом `Job.cancel()`.
 *
 * 2. **Автоматическая отмена**: Метод `executeLLMRequest()` автоматически отменяет
 *    предыдущий запрос перед запуском нового. Это гарантирует, что:
 *    - В любой момент времени выполняется максимум один запрос к LLM
 *    - Результаты отмененных запросов не попадают в UI
 *    - Состояние загрузки (isTyping) всегда корректно
 *
 * 3. **Кооперативная отмена**: Kotlin coroutines поддерживают кооперативную отмену.
 *    При вызове `Job.cancel()`:
 *    - Корутина получает CancellationException при следующей suspend точке
 *    - Ktor HttpClient автоматически прерывает HTTP запрос
 *    - finally блок гарантирует сброс состояния загрузки
 *
 * 4. **Цепочка отмены**: Отмена распространяется через всю цепочку вызовов:
 *    ChatStoreFactory → Use Case → Repository → DataSource → API Client
 *
 * ### Пример использования:
 *
 * ```kotlin
 * // Пользователь отправляет сообщение
 * executeLLMRequest {
 *     sendMessageUseCase("Hello")  // Запрос 1 начинается
 * }
 *
 * // Пользователь очищает чат до завершения Запроса 1
 * cancelActiveRequest()  // Запрос 1 отменяется
 * clearChatUseCase()
 *
 * // Результат Запроса 1 никогда не попадет в UI
 * ```
 *
 * @see executeLLMRequest Безопасное выполнение запросов с автоматической отменой
 * @see cancelActiveRequest Явная отмена активного запроса
 */
internal class ChatStoreFactory(
    private val storeFactory: StoreFactory,
    private val chatRepository: ChatRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendSystemPromptUseCase: SendSystemPromptUseCase,
    private val clearChatUseCase: ClearChatUseCase,
    private val summarizeChatUseCase: domain.usecase.chat.SummarizeChatUseCase,
    private val settingsRepository: data.repository.SettingsRepository
) {

    sealed interface Action {
        data object InitAction : Action
    }

    fun create(): ChatStore =
        object : ChatStore, Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> by storeFactory.create(
            name = "ChatStore",
            initialState = ChatStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
            bootstrapper = SimpleBootstrapper(
                Action.InitAction
            )
        ) {}

    private sealed interface Message {
        data class MessagesUpdated(val messages: List<domain.Message>) : Message
        data class InputUpdated(val text: String) : Message
        data class TypingUpdated(val isTyping: Boolean) : Message
        data class SelectedModelUpdated(val model: domain.LlmModel) : Message
        data class SelectedAgentUpdated(val agent: domain.Agent?) : Message
        data class TokenCountUpdated(val tokenCount: Int?) : Message
        data object CleanTokensCount : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ChatStore.Intent, Action, ChatStore.State, Message, Nothing>() {

        // Получаем TokenCounter при инициализации (может быть null на платформах без поддержки)
        private val tokenCounter = getTokenCounter()

        /**
         * Job активного запроса к LLM.
         * Используется для отмены запроса при очистке чата или смене агента.
         */
        private var activeRequestJob: kotlinx.coroutines.Job? = null

        /**
         * Отменяет активный запрос к LLM, если он существует.
         * Также сбрасывает состояние загрузки.
         */
        private fun cancelActiveRequest() {
            activeRequestJob?.cancel()
            activeRequestJob = null
            dispatch(TypingUpdated(false))
        }

        /**
         * Безопасно выполняет запрос к LLM с управлением состоянием загрузки.
         * Автоматически отменяет предыдущий запрос, если он еще выполняется.
         *
         * @param block Suspend функция, которая выполняет запрос
         */
        private fun executeLLMRequest(block: suspend () -> Unit) {
            // Отменяем предыдущий запрос, если он еще выполняется
            cancelActiveRequest()

            // Запускаем новый запрос
            activeRequestJob = scope.launch {
                dispatch(TypingUpdated(true))
                try {
                    block()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // Запрос был отменен - это нормально, не логируем
                    println("Запрос отменен")
                } catch (e: Exception) {
                    // Неожиданная ошибка
                    println("Неожиданная ошибка при выполнении запроса: ${e.message}")
                } finally {
                    // Всегда сбрасываем состояние загрузки
                    dispatch(TypingUpdated(false))
                }
            }
        }

        override fun executeAction(action: Action) {
            super.executeAction(action)

            when (action) {
                Action.InitAction -> {
                    // Подписка на изменения сообщений
                    scope.launch {
                        chatRepository.messages.collect { messages ->
                            dispatch(Message.MessagesUpdated(messages))
                        }
                    }

                    // Подписка на изменения настроек для system prompt
                    /*scope.launch {
                        dispatch(Message.TypingUpdated(true))
                        chatRepository.sendWelcomeMessage()
                        dispatch(Message.TypingUpdated(false))
                    }*/
                }
            }
        }

        override fun executeIntent(intent: ChatStore.Intent) {
            when (intent) {
                is ChatStore.Intent.UpdateInput -> {
                    if (intent.text.length <= MAX_MESSAGE_LENGTH) {
                        dispatch(InputUpdated(intent.text))

                        // Подсчитываем токены для введенного текста
                        val tokenCount = if (intent.text.isNotBlank()) {
                            tokenCounter?.countTokens(intent.text, state().selectedModel)
                        } else {
                            null
                        }
                        dispatch(TokenCountUpdated(tokenCount))
                    }
                }

                is ChatStore.Intent.SendMessage -> {
                    val messageText = state().input.trim()
                    if (messageText.isEmpty()) return

                    // Очищаем поле ввода
                    dispatch(InputUpdated(""))
                    dispatch(TokenCountUpdated(null))

                    // Используем безопасный wrapper для выполнения запроса
                    executeLLMRequest {
                        sendMessageUseCase(messageText)
                            .onFailure { error ->
                                println("Ошибка отправки сообщения: ${error.message}")
                            }
                    }
                }

                is ChatStore.Intent.ClearChat -> {
                    // КРИТИЧНО: Отменяем активный запрос перед очисткой чата
                    cancelActiveRequest()

                    // Используем Use Case для очистки с обработкой Result
                    clearChatUseCase()
                        .onFailure { error ->
                            println("Ошибка очистки чата: ${error.message}")
                        }
                    dispatch(InputUpdated(""))
                    dispatch(TokenCountUpdated(null))
                    dispatch(CleanTokensCount)

                    // Если выбран агент, отправляем его системный промпт
                    state().selectedAgent?.let { agent ->
                        executeLLMRequest {
                            sendSystemPromptUseCase(agent.systemPrompt)
                                .onFailure { error ->
                                    println("Ошибка отправки системного промпта: ${error.message}")
                                }
                        }
                    }
                }

                is ChatStore.Intent.SummarizeChat -> {
                    // Отменяем активный запрос перед сжатием
                    cancelActiveRequest()

                    // Очищаем поле ввода
                    dispatch(InputUpdated(""))
                    dispatch(TokenCountUpdated(null))

                    // Используем безопасный wrapper для выполнения запроса
                    executeLLMRequest {
                        summarizeChatUseCase()
                            .onFailure { error ->
                                println("Ошибка сжатия истории: ${error.message}")
                            }
                    }
                }

                is ChatStore.Intent.SelectAgent -> {
                    // КРИТИЧНО: Отменяем активный запрос перед сменой агента
                    cancelActiveRequest()

                    // Очищаем чат при смене агента с обработкой Result
                    clearChatUseCase()
                        .onFailure { error ->
                            println("Ошибка очистки чата при смене агента: ${error.message}")
                        }
                    dispatch(InputUpdated(""))
                    dispatch(TokenCountUpdated(null))
                    dispatch(SelectedAgentUpdated(intent.agent))

                    // Отправляем системный промпт выбранного агента
                    executeLLMRequest {
                        sendSystemPromptUseCase(intent.agent.systemPrompt)
                            .onFailure { error ->
                                println("Ошибка отправки системного промпта агента: ${error.message}")
                            }
                    }
                }

                is ChatStore.Intent.SelectLlmModel -> {
                    // Сохраняем выбранную модель в настройках
                    settingsRepository.updateSelectedLlmModel(intent.model)
                    dispatch(
                        message = SelectedModelUpdated(model = intent.model)
                    )

                    // Пересчитываем токены для текущего ввода с новой моделью
                    val currentInput = state().input
                    if (currentInput.isNotBlank()) {
                        val tokenCount = tokenCounter?.countTokens(currentInput, intent.model)
                        dispatch(TokenCountUpdated(tokenCount))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ChatStore.State, Message> {
        override fun ChatStore.State.reduce(msg: Message): ChatStore.State =
            when (msg) {
                is MessagesUpdated -> {
                    // Подсчитываем общую статистику токенов по всем сообщениям
                    val totalPrompt = msg.messages.sumOf { it.promptTokens ?: 0 }
                    val totalCompletion = msg.messages.sumOf { it.completionTokens ?: 0 }
                    val total = msg.messages.sumOf { it.totalTokens ?: 0 }

                    copy(
                        messages = msg.messages,
                        totalPromptTokens = totalPrompt,
                        totalCompletionTokens = totalCompletion,
                        totalTokens = total
                    )
                }
                is InputUpdated -> copy(input = msg.text)
                is TypingUpdated -> copy(isTyping = msg.isTyping)
                is SelectedModelUpdated -> copy(selectedModel = msg.model)
                is SelectedAgentUpdated -> copy(selectedAgent = msg.agent)
                is TokenCountUpdated -> copy(inputTokenCount = msg.tokenCount)
                CleanTokensCount -> copy(
                    totalPromptTokens = 0,
                    totalCompletionTokens = 0,
                    totalTokens = 0
                )
            }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}