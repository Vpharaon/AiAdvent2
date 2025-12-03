package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import mvi.eventplanner.EventPlannerStore
import mvi.eventplanner.EventPlannerStoreFactory

interface EventPlannerComponent {
    val state: StateFlow<EventPlannerStore.State>

    fun onInputChange(text: String)
    fun onSendClick()
    fun onClearClick()
    fun onTabSelect(tab: EventPlannerStore.EventPlanTab)
    fun onBackClick()
}

class DefaultEventPlannerComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    chatRepository: ChatRepository,
    private val onNavigateBack: () -> Unit
) : EventPlannerComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        EventPlannerStoreFactory(
            storeFactory = storeFactory,
            chatRepository = chatRepository
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<EventPlannerStore.State> = store.stateFlow

    override fun onInputChange(text: String) {
        store.accept(EventPlannerStore.Intent.UpdateInput(text))
    }

    override fun onSendClick() {
        store.accept(EventPlannerStore.Intent.SendMessage)
    }

    override fun onClearClick() {
        store.accept(EventPlannerStore.Intent.ClearChat)
    }

    override fun onTabSelect(tab: EventPlannerStore.EventPlanTab) {
        store.accept(EventPlannerStore.Intent.SelectTab(tab))
    }

    override fun onBackClick() {
        onNavigateBack()
    }
}