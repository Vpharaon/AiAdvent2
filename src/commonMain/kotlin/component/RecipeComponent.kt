package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import data.repository.StructuredChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import mvi.recipe.RecipeStore
import mvi.recipe.RecipeStoreFactory

interface RecipeComponent {
    val state: StateFlow<RecipeStore.State>

    fun onDishNameChange(name: String)
    fun onGetRecipeClick()
    fun onTabSelect(tab: RecipeStore.RecipeTab)
    fun onBackClick()
}

class DefaultRecipeComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    structuredChatRepository: StructuredChatRepository,
    private val onNavigateBack: () -> Unit
) : RecipeComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        RecipeStoreFactory(
            storeFactory = storeFactory,
            structuredChatRepository = structuredChatRepository
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RecipeStore.State> = store.stateFlow

    override fun onDishNameChange(name: String) {
        store.accept(RecipeStore.Intent.UpdateDishName(name))
    }

    override fun onGetRecipeClick() {
        store.accept(RecipeStore.Intent.GetRecipe)
    }

    override fun onTabSelect(tab: RecipeStore.RecipeTab) {
        store.accept(RecipeStore.Intent.SelectTab(tab))
    }

    override fun onBackClick() {
        onNavigateBack()
    }
}