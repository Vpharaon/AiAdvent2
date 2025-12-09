package mvi.chat

import com.arkivanov.mvikotlin.core.store.Store
import domain.LlmModel
import domain.LlmModels
import domain.Message

interface ChatStore : Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> {

    sealed interface Intent {
        data class UpdateInput(val text: String) : Intent
        data object SendMessage : Intent
        data object ClearChat : Intent
        data class UpdateSystemPrompt(val systemPrompt: String) : Intent
        data object SaveSystemPrompt : Intent
        data object ToggleSystemPromptEditor : Intent
        data class SelectLlmModel(val model: LlmModel) : Intent
    }

    sealed interface Label

    data class State(
        val messages: List<Message> = emptyList(),
        val input: String = "",
        val isTyping: Boolean = false,
        val systemPrompt: String = "",
        val isSystemPromptExpanded: Boolean = false,
        val availableModels: List<LlmModel> = LlmModels.DEFAULT_MODELS,
        val selectedModel: LlmModel = LlmModels.DEFAULT_MODELS.first()
    )
}