package mvi.eventplanner

import com.arkivanov.mvikotlin.core.store.Store
import domain.Message
import domain.structured.EventPlanWithRaw
import mvi.eventplanner.EventPlannerStore.Intent
import mvi.eventplanner.EventPlannerStore.State

interface EventPlannerStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class UpdateInput(val text: String) : Intent
        data object SendMessage : Intent
        data object ClearChat : Intent
        data class SelectTab(val tab: EventPlanTab) : Intent
    }

    data class State(
        val messages: List<Message> = emptyList(),
        val input: String = "",
        val isTyping: Boolean = false,
        val eventPlan: EventPlanWithRaw? = null,
        val errorMessage: String? = null,
        val selectedTab: EventPlanTab = EventPlanTab.CHAT
    )

    enum class EventPlanTab {
        CHAT, PLAN, RAW_JSON, FULL_RESPONSE
    }
}