package mvi.eventplanner

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.ChatRepository
import data.parser.StructuredResponseParser
import data.prompt.StructuredPromptBuilder
import data.network.model.ChatMessage
import data.network.model.MessageRole
import domain.Message
import domain.structured.EventPlanWithRaw
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mvi.eventplanner.EventPlannerStore.EventPlanTab

internal class EventPlannerStoreFactory(
    private val storeFactory: StoreFactory,
    private val chatRepository: ChatRepository
) {
    private val parser = StructuredResponseParser()
    private val promptBuilder = StructuredPromptBuilder()
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val conversationHistory = mutableListOf<ChatMessage>()

    fun create(): EventPlannerStore =
        object : EventPlannerStore, Store<EventPlannerStore.Intent, EventPlannerStore.State, Nothing> by storeFactory.create(
            name = "EventPlannerStore",
            initialState = EventPlannerStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {
        data class MessagesUpdated(val messages: List<domain.Message>) : Message
        data class InputUpdated(val text: String) : Message
        data class TypingUpdated(val isTyping: Boolean) : Message
        data class EventPlanUpdated(val plan: EventPlanWithRaw?) : Message
        data class ErrorUpdated(val error: String?) : Message
        data class TabSelected(val tab: EventPlanTab) : Message
    }

    private inner class ExecutorImpl : CoroutineExecutor<EventPlannerStore.Intent, Nothing, EventPlannerStore.State, Message, Nothing>() {
        override fun executeAction(action: Nothing) {
            // Подписка на изменения сообщений
            scope.launch {
                chatRepository.messages.collect { messages ->
                    dispatch(Message.MessagesUpdated(messages))
                }
            }

            // Инициализируем диалог с системным промптом
            scope.launch {
                startConversation()
            }
        }

        private suspend fun startConversation() {
            dispatch(Message.TypingUpdated(true))

            try {
                // Добавляем системный промпт
                val systemPrompt = promptBuilder.buildEventPlannerPrompt()
                conversationHistory.add(
                    ChatMessage(
                        role = MessageRole.SYSTEM,
                        content = systemPrompt
                    )
                )

                // Добавляем начальное сообщение пользователя
                val initialUserMessage = "Здравствуйте! Я хочу организовать новогодний корпоратив для нашей компании."
                conversationHistory.add(
                    ChatMessage(
                        role = MessageRole.USER,
                        content = initialUserMessage
                    )
                )

                // Получаем первое сообщение от менеджера
                val result = chatRepository.sendMessageWithHistory(conversationHistory)

                result.onSuccess { response ->
                    val assistantMessage = response.choices?.firstOrNull()?.message?.content

                    if (assistantMessage != null) {
                        conversationHistory.add(
                            ChatMessage(
                                role = MessageRole.ASSISTANT,
                                content = assistantMessage
                            )
                        )
                        chatRepository.addAssistantMessage(assistantMessage)
                    } else {
                        dispatch(Message.ErrorUpdated("Не удалось получить приветствие от менеджера"))
                    }
                }.onFailure { error ->
                    dispatch(Message.ErrorUpdated("Ошибка инициализации: ${error.message}"))
                }
            } catch (e: Exception) {
                dispatch(Message.ErrorUpdated("Ошибка: ${e.message}"))
            } finally {
                dispatch(Message.TypingUpdated(false))
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

                        try {
                            conversationHistory.add(
                                ChatMessage(
                                    role = MessageRole.USER,
                                    content = messageText
                                )
                            )

                            chatRepository.addUserMessage(messageText)

                            val result = chatRepository.sendMessageWithHistory(conversationHistory)

                            result.onSuccess { response ->
                                val assistantMessage = response.choices?.firstOrNull()?.message?.content

                                if (assistantMessage != null) {
                                    conversationHistory.add(
                                        ChatMessage(
                                            role = MessageRole.ASSISTANT,
                                            content = assistantMessage
                                        )
                                    )

                                    val parseResult = parser.parseEventPlanWithRaw(assistantMessage)

                                    if (parseResult.isSuccess) {
                                        val eventPlanData = parseResult.getOrNull()!!
                                        val fullResponseJson = json.encodeToString(response)

                                        dispatch(Message.EventPlanUpdated(eventPlanData.copy(fullResponseJson = fullResponseJson)))
                                        dispatch(Message.TabSelected(EventPlanTab.PLAN))
                                        chatRepository.addAssistantMessage(assistantMessage)
                                    } else {
                                        chatRepository.addAssistantMessage(assistantMessage)
                                    }
                                } else {
                                    dispatch(Message.ErrorUpdated("Получен пустой ответ от сервера"))
                                }
                            }.onFailure { error ->
                                dispatch(Message.ErrorUpdated("Ошибка: ${error.message}"))
                                if (conversationHistory.isNotEmpty()) {
                                    conversationHistory.removeAt(conversationHistory.size - 1)
                                }
                            }
                        } catch (e: Exception) {
                            dispatch(Message.ErrorUpdated("Ошибка: ${e.message}"))
                        } finally {
                            dispatch(Message.TypingUpdated(false))
                        }
                    }
                }

                is EventPlannerStore.Intent.ClearChat -> {
                    chatRepository.clearMessages()
                    conversationHistory.clear()
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