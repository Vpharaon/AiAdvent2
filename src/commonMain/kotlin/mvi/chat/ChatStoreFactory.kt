package mvi.chat

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.ChatRepository
import domain.Message
import kotlinx.coroutines.launch

internal class ChatStoreFactory(
    private val storeFactory: StoreFactory,
    private val chatRepository: ChatRepository
) {

    fun create(): ChatStore =
        object : ChatStore, Store<ChatStore.Intent, ChatStore.State, Nothing> by storeFactory.create(
            name = "ChatStore",
            initialState = ChatStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {
        data class MessagesUpdated(val messages: List<domain.Message>) : Message
        data class InputUpdated(val text: String) : Message
        data class TypingUpdated(val isTyping: Boolean) : Message
    }

    private inner class ExecutorImpl : CoroutineExecutor<ChatStore.Intent, Nothing, ChatStore.State, Message, Nothing>() {
        override fun executeAction(action: Nothing) {
            // Подписка на изменения сообщений
            scope.launch {
                chatRepository.messages.collect { messages ->
                    dispatch(Message.MessagesUpdated(messages))
                }
            }

            // Отправляем приветственное сообщение
            scope.launch {
                dispatch(Message.TypingUpdated(true))
                chatRepository.sendWelcomeMessage()
                dispatch(Message.TypingUpdated(false))
            }
        }

        override fun executeIntent(intent: ChatStore.Intent) {
            when (intent) {
                is ChatStore.Intent.UpdateInput -> {
                    if (intent.text.length <= MAX_MESSAGE_LENGTH) {
                        dispatch(Message.InputUpdated(intent.text))
                    }
                }

                is ChatStore.Intent.SendMessage -> {
                    val messageText = state().input.trim()
                    if (messageText.isEmpty()) return

                    // Очищаем поле ввода
                    dispatch(Message.InputUpdated(""))

                    scope.launch {
                        dispatch(Message.TypingUpdated(true))
                        chatRepository.sendUserText(messageText)
                        dispatch(Message.TypingUpdated(false))
                    }
                }

                is ChatStore.Intent.ClearChat -> {
                    chatRepository.clearMessages()
                    dispatch(Message.InputUpdated(""))

                    scope.launch {
                        dispatch(Message.TypingUpdated(true))
                        chatRepository.sendWelcomeMessage()
                        dispatch(Message.TypingUpdated(false))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ChatStore.State, Message> {
        override fun ChatStore.State.reduce(msg: Message): ChatStore.State =
            when (msg) {
                is Message.MessagesUpdated -> copy(messages = msg.messages)
                is Message.InputUpdated -> copy(input = msg.text)
                is Message.TypingUpdated -> copy(isTyping = msg.isTyping)
            }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}