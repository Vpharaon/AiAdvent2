package mvi.chat

import com.arkivanov.mvikotlin.core.store.Store
import domain.Message

interface ChatStore : Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> {

    sealed interface Intent {
        data class UpdateInput(val text: String) : Intent
        data object SendMessage : Intent
        data object ClearChat : Intent
    }

    sealed interface Label

    data class State(
        val messages: List<Message> = emptyList(),
        val input: String = "",
        val isTyping: Boolean = false
    )
}