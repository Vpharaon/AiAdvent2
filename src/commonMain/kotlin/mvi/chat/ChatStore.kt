package mvi.chat

import com.arkivanov.mvikotlin.core.store.Store
import domain.Agent
import domain.LlmModel
import domain.LlmModels
import domain.Message

interface ChatStore : Store<ChatStore.Intent, ChatStore.State, ChatStore.Label> {

    sealed interface Intent {
        data class UpdateInput(val text: String) : Intent
        data object SendMessage : Intent
        data object ClearChat : Intent
        data object SummarizeChat : Intent
        data class SelectLlmModel(val model: LlmModel) : Intent
        data class SelectAgent(val agent: Agent) : Intent
    }

    sealed interface Label

    data class State(
        val messages: List<Message> = emptyList(),
        val input: String = "",
        val isTyping: Boolean = false,
        val availableModels: List<LlmModel> = LlmModels.DEFAULT_MODELS,
        val selectedModel: LlmModel = LlmModels.DEFAULT_MODELS.first(),
        val availableAgents: List<Agent> = Agent.getAll(),
        val selectedAgent: Agent? = Agent.getAll().first(),
        val inputTokenCount: Int? = null,
        val totalPromptTokens: Int = 0,
        val totalCompletionTokens: Int = 0,
        val totalTokens: Int = 0
    )
}