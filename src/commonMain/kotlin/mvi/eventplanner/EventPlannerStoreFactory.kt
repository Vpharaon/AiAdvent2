package mvi.eventplanner

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.EventPlannerRepository
import domain.Message
import domain.structured.EventPlanWithRaw
import domain.usecase.eventplanner.ClearEventPlanChatUseCase
import domain.usecase.eventplanner.SendEventPlanMessageUseCase
import domain.usecase.eventplanner.StartEventPlanConversationUseCase
import kotlinx.coroutines.launch
import mvi.eventplanner.EventPlannerStore.EventPlanTab

/**
 * Factory для создания EventPlannerStore.
 * Использует Use Cases для выполнения бизнес-логики.
 */
internal class EventPlannerStoreFactory(
    private val storeFactory: StoreFactory,
    private val eventPlannerRepository: EventPlannerRepository,
    private val sendMessageUseCase: SendEventPlanMessageUseCase,
    private val startConversationUseCase: StartEventPlanConversationUseCase,
    private val clearChatUseCase: ClearEventPlanChatUseCase
) {

    fun create(): EventPlannerStore =
        object : EventPlannerStore,
            Store<EventPlannerStore.Intent, EventPlannerStore.State, Nothing> by storeFactory.create(
                name = "EventPlannerStore",
                initialState = EventPlannerStore.State(),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
                bootstrapper = SimpleBootstrapper(
                    Action.InitAction
                )
            ) {}

    private sealed interface Action {
        data object InitAction : Action
    }

    private sealed interface Message {
        data class MessagesUpdated(val messages: List<domain.Message>) : Message
        data class InputUpdated(val text: String) : Message
        data class TypingUpdated(val isTyping: Boolean) : Message
        data class EventPlanUpdated(val plan: EventPlanWithRaw?) : Message
        data class ErrorUpdated(val error: String?) : Message
        data class TabSelected(val tab: EventPlanTab) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<EventPlannerStore.Intent, Action, EventPlannerStore.State, Message, Nothing>() {

        override fun executeAction(action: Action) {
            super.executeAction(action)
            when (action) {
                Action.InitAction -> {
                    // Подписываемся на изменения сообщений
                    scope.launch {
                        eventPlannerRepository.messages.collect { messages ->
                            dispatch(Message.MessagesUpdated(messages))
                        }
                    }

                    // Инициализируем диалог с системным промптом
                    scope.launch {
                        startConversation()
                    }
                }
            }
        }

        override fun executeIntent(intent: EventPlannerStore.Intent) {
            when (intent) {
                is EventPlannerStore.Intent.UpdateInput -> {
                    if (intent.text.length <= MAX_MESSAGE_LENGTH) {
                        dispatch(Message.InputUpdated(intent.text))
                    }
                }

                is EventPlannerStore.Intent.SendMessage -> {
                    val messageText = state().input.trim()
                    if (messageText.isEmpty()) return

                    dispatch(Message.InputUpdated(""))

                    scope.launch {
                        dispatch(Message.TypingUpdated(true))
                        dispatch(Message.ErrorUpdated(null))

                        // Используем Use Case для отправки сообщения
                        val result = sendMessageUseCase(messageText)

                        result.onSuccess { eventPlanData ->
                            // Если получен распарсенный план, обновляем состояние
                            if (eventPlanData != null) {
                                dispatch(Message.EventPlanUpdated(eventPlanData))
                                dispatch(Message.TabSelected(EventPlanTab.PLAN))
                            }
                        }.onFailure { error ->
                            dispatch(Message.ErrorUpdated("Ошибка: ${error.message}"))
                        }

                        dispatch(Message.TypingUpdated(false))
                    }
                }

                is EventPlannerStore.Intent.ClearChat -> {
                    // Используем Use Case для очистки
                    clearChatUseCase()
                    dispatch(Message.InputUpdated(""))
                    dispatch(Message.EventPlanUpdated(null))
                    dispatch(Message.ErrorUpdated(null))
                    dispatch(Message.TabSelected(EventPlanTab.CHAT))

                    scope.launch {
                        startConversation()
                    }
                }

                is EventPlannerStore.Intent.SelectTab -> {
                    dispatch(Message.TabSelected(intent.tab))
                }
            }
        }

        private suspend fun startConversation() {
            dispatch(Message.TypingUpdated(true))

            // Используем Use Case для инициализации диалога
            val result = startConversationUseCase()

            result.onSuccess {
                // Сообщения уже добавлены через flow из Repository
            }.onFailure { error ->
                dispatch(Message.ErrorUpdated("Ошибка инициализации: ${error.message}"))
            }

            dispatch(Message.TypingUpdated(false))
        }
    }

    private object ReducerImpl : Reducer<EventPlannerStore.State, Message> {
        override fun EventPlannerStore.State.reduce(msg: Message): EventPlannerStore.State =
            when (msg) {
                is Message.MessagesUpdated -> copy(messages = msg.messages)
                is Message.InputUpdated -> copy(input = msg.text)
                is Message.TypingUpdated -> copy(isTyping = msg.isTyping)
                is Message.EventPlanUpdated -> copy(eventPlan = msg.plan)
                is Message.ErrorUpdated -> copy(errorMessage = msg.error)
                is Message.TabSelected -> copy(selectedTab = msg.tab)
            }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}