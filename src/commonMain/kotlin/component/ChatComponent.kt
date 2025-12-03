package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import mvi.chat.ChatStore
import mvi.chat.ChatStoreFactory

interface ChatComponent {
    val state: StateFlow<ChatStore.State>

    fun onInputChange(text: String)
    fun onSendClick()
    fun onClearClick()
    fun onBackClick()
    fun onSettingsClick()
}

class DefaultChatComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    chatRepository: ChatRepository,
    private val onNavigateBack: () -> Unit,
    private val onNavigateToSettings: () -> Unit
) : ChatComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        ChatStoreFactory(
            storeFactory = storeFactory,
            chatRepository = chatRepository
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

    override fun onBackClick() {
        onNavigateBack()
    }

    override fun onSettingsClick() {
        onNavigateToSettings()
    }
}