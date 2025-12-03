package mvi.recipe

import com.arkivanov.mvikotlin.core.store.Store
import domain.structured.RecipeWithRaw
import mvi.recipe.RecipeStore.Intent
import mvi.recipe.RecipeStore.State

interface RecipeStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class UpdateDishName(val name: String) : Intent
        data object GetRecipe : Intent
        data class SelectTab(val tab: RecipeTab) : Intent
    }

    data class State(
        val dishName: String = "",
        val recipeData: RecipeWithRaw? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val selectedTab: RecipeTab = RecipeTab.FORMATTED
    )

    enum class RecipeTab {
        FORMATTED, RAW_JSON, FULL_RESPONSE
    }
}