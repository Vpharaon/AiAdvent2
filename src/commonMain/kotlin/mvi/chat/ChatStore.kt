package mvi.chat

import com.arkivanov.mvikotlin.core.store.Store
import domain.Message
import mvi.chat.ChatStore.Intent
import mvi.chat.ChatStore.State

interface ChatStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class UpdateInput(val text: String) : Intent
        data object SendMessage : Intent
        data object ClearChat : Intent
    }

    data class State(
        val messages: List<Message> = emptyList(),
        val input: String = "",
        val isTyping: Boolean = false
    )
}