package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import data.repository.ChatRepository
import domain.Agent
import domain.LlmModel
import domain.usecase.chat.ClearChatUseCase
import domain.usecase.chat.SendMessageUseCase
import domain.usecase.chat.SendSystemPromptUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import mvi.chat.ChatStore
import mvi.chat.ChatStoreFactory

interface ChatComponent {
    val state: StateFlow<ChatStore.State>

    fun onInputChange(text: String)
    fun onSendClick()
    fun onClearClick()
    fun onSummaryClick()
    fun onSelectLlmModel(model: LlmModel)
    fun onSelectAgent(agent: Agent)
    fun onSettingsClick()
}

class DefaultChatComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    chatRepository: ChatRepository,
    sendMessageUseCase: SendMessageUseCase,
    sendSystemPromptUseCase: SendSystemPromptUseCase,
    clearChatUseCase: ClearChatUseCase,
    summarizeChatUseCase: domain.usecase.chat.SummarizeChatUseCase,
    settingsRepository: data.repository.SettingsRepository,
    private val onNavigateToSettings: () -> Unit
) : ChatComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        ChatStoreFactory(
            storeFactory = storeFactory,
            chatRepository = chatRepository,
            sendMessageUseCase = sendMessageUseCase,
            sendSystemPromptUseCase = sendSystemPromptUseCase,
            clearChatUseCase = clearChatUseCase,
            summarizeChatUseCase = summarizeChatUseCase,
            settingsRepository = settingsRepository
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ChatStore.State> = store.stateFlow

    override fun onInputChange(text: String) {
        store.accept(ChatStore.Intent.UpdateInput(text))
    }

    override fun onSendClick() {
        store.accept(ChatStore.Intent.SendMessage)
    }

    override fun onClearClick() {
        store.accept(ChatStore.Intent.ClearChat)
    }

    override fun onSummaryClick() {
        store.accept(ChatStore.Intent.SummarizeChat)
    }

    override fun onSelectLlmModel(model: LlmModel) {
        store.accept(ChatStore.Intent.SelectLlmModel(model))
    }

    override fun onSelectAgent(agent: Agent) {
        store.accept(ChatStore.Intent.SelectAgent(agent))
    }

    override fun onSettingsClick() {
        onNavigateToSettings()
    }
}