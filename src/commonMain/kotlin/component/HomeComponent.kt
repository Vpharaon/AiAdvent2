package component

import com.arkivanov.decompose.ComponentContext

interface HomeComponent {
    fun onChatClick()
    fun onRecipesClick()
    fun onEventPlannerClick()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onNavigateToChat: () -> Unit,
    private val onNavigateToRecipes: () -> Unit,
    private val onNavigateToEventPlanner: () -> Unit
) : HomeComponent, ComponentContext by componentContext {

    override fun onChatClick() {
        onNavigateToChat()
    }

    override fun onRecipesClick() {
        onNavigateToRecipes()
    }

    override fun onEventPlannerClick() {
        onNavigateToEventPlanner()
    }
}