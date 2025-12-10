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
 */
internal class ChatStoreFactory(
    private val storeFactory: StoreFactory,
    private val chatRepository: ChatRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendSystemPromptUseCase: SendSystemPromptUseCase,
    private val clearChatUseCase: ClearChatUseCase,
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
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ChatStore.Intent, Action, ChatStore.State, Message, Nothing>() {

        // Получаем TokenCounter при инициализации (может быть null на платформах без поддержки)
        private val tokenCounter = getTokenCounter()

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

                    scope.launch {
                        dispatch(TypingUpdated(true))
                        // Используем Use Case с обработкой Result
                        sendMessageUseCase(messageText)
                            .onFailure { error ->
                                // Логируем ошибку (в production можно добавить аналитику)
                                println("Ошибка отправки сообщения: ${error.message}")
                            }
                        dispatch(TypingUpdated(false))
                    }
                }

                is ChatStore.Intent.ClearChat -> {
                    // Используем Use Case для очистки с обработкой Result
                    clearChatUseCase()
                        .onFailure { error ->
                            println("Ошибка очистки чата: ${error.message}")
                        }
                    dispatch(InputUpdated(""))
                    dispatch(TokenCountUpdated(null))

                    // Если выбран агент, отправляем его системный промпт
                    state().selectedAgent?.let { agent ->
                        scope.launch {
                            dispatch(TypingUpdated(true))
                            sendSystemPromptUseCase(agent.systemPrompt)
                                .onFailure { error ->
                                    println("Ошибка отправки системного промпта: ${error.message}")
                                }
                            dispatch(TypingUpdated(false))
                        }
                    }
                }

                is ChatStore.Intent.SelectAgent -> {
                    // Очищаем чат при смене агента с обработкой Result
                    clearChatUseCase()
                        .onFailure { error ->
                            println("Ошибка очистки чата при смене агента: ${error.message}")
                        }
                    dispatch(InputUpdated(""))
                    dispatch(TokenCountUpdated(null))
                    dispatch(SelectedAgentUpdated(intent.agent))

                    // Отправляем системный промпт выбранного агента
                    scope.launch {
                        dispatch(TypingUpdated(true))
                        sendSystemPromptUseCase(intent.agent.systemPrompt)
                            .onFailure { error ->
                                println("Ошибка отправки системного промпта агента: ${error.message}")
                            }
                        dispatch(TypingUpdated(false))
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
            }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}