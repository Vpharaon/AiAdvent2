package mvi.chat

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.ChatRepository
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
    private val clearChatUseCase: ClearChatUseCase
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
        data class SystemPromptUpdated(val systemPrompt: String) : Message
        data class SystemPromptExpandedUpdated(val isExpanded: Boolean) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ChatStore.Intent, Action, ChatStore.State, Message, Nothing>() {

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
                    }
                }

                is ChatStore.Intent.SendMessage -> {
                    val messageText = state().input.trim()
                    if (messageText.isEmpty()) return

                    // Очищаем поле ввода
                    dispatch(InputUpdated(""))

                    scope.launch {
                        dispatch(TypingUpdated(true))
                        // Используем Use Case вместо прямого вызова Repository
                        sendMessageUseCase(messageText)
                        dispatch(TypingUpdated(false))
                    }
                }

                is ChatStore.Intent.ClearChat -> {
                    // Используем Use Case для очистки
                    clearChatUseCase()
                    dispatch(InputUpdated(""))

                    scope.launch {
                        dispatch(TypingUpdated(true))
                        // Используем Use Case для отправки system prompt
                        sendSystemPromptUseCase(state().systemPrompt)
                        dispatch(TypingUpdated(false))
                    }
                }

                ChatStore.Intent.SaveSystemPrompt -> {
                    scope.launch {
                        dispatch(TypingUpdated(true))
                        // Используем Use Case для отправки system prompt
                        sendSystemPromptUseCase(state().systemPrompt)
                        dispatch(TypingUpdated(false))
                    }
                }

                is ChatStore.Intent.UpdateSystemPrompt -> {
                    dispatch(
                        message = SystemPromptUpdated(systemPrompt = intent.systemPrompt)
                    )
                }

                ChatStore.Intent.ToggleSystemPromptEditor -> {
                    dispatch(
                        message = SystemPromptExpandedUpdated(isExpanded = !state().isSystemPromptExpanded)
                    )
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ChatStore.State, Message> {
        override fun ChatStore.State.reduce(msg: Message): ChatStore.State =
            when (msg) {
                is MessagesUpdated -> copy(messages = msg.messages)
                is InputUpdated -> copy(input = msg.text)
                is TypingUpdated -> copy(isTyping = msg.isTyping)
                is SystemPromptUpdated -> copy(systemPrompt = msg.systemPrompt)
                is SystemPromptExpandedUpdated -> copy(isSystemPromptExpanded = msg.isExpanded)
            }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}