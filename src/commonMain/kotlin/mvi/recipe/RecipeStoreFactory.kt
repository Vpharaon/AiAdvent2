package mvi.recipe

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.StructuredChatRepository
import domain.structured.RecipeWithRaw
import kotlinx.coroutines.launch
import mvi.recipe.RecipeStore.RecipeTab

internal class RecipeStoreFactory(
    private val storeFactory: StoreFactory,
    private val structuredChatRepository: StructuredChatRepository
) {

    fun create(): RecipeStore =
        object : RecipeStore, Store<RecipeStore.Intent, RecipeStore.State, Nothing> by storeFactory.create(
            name = "RecipeStore",
            initialState = RecipeStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {
        data class DishNameUpdated(val name: String) : Message
        data class LoadingUpdated(val isLoading: Boolean) : Message
        data class RecipeDataUpdated(val data: RecipeWithRaw?) : Message
        data class ErrorUpdated(val error: String?) : Message
        data class TabSelected(val tab: RecipeTab) : Message
    }

    private inner class ExecutorImpl : CoroutineExecutor<RecipeStore.Intent, Nothing, RecipeStore.State, Message, Nothing>() {
        override fun executeIntent(intent: RecipeStore.Intent) {
            when (intent) {
                is RecipeStore.Intent.UpdateDishName -> {
                    dispatch(Message.DishNameUpdated(intent.name))
                }

                is RecipeStore.Intent.GetRecipe -> {
                    val dishName = state().dishName
                    if (dishName.isBlank()) return

                    dispatch(Message.LoadingUpdated(true))
                    dispatch(Message.ErrorUpdated(null))
                    dispatch(Message.RecipeDataUpdated(null))

                    scope.launch {
                        val result = structuredChatRepository.getRecipeWithRaw(dishName)
                        dispatch(Message.LoadingUpdated(false))

                        result.onSuccess { data ->
                            dispatch(Message.RecipeDataUpdated(data))
                            dispatch(Message.TabSelected(RecipeTab.FORMATTED))
                        }.onFailure { error ->
                            dispatch(Message.ErrorUpdated("Ошибка: ${error.message}"))
                        }
                    }
                }

                is RecipeStore.Intent.SelectTab -> {
                    dispatch(Message.TabSelected(intent.tab))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RecipeStore.State, Message> {
        override fun RecipeStore.State.reduce(msg: Message): RecipeStore.State =
            when (msg) {
                is Message.DishNameUpdated -> copy(dishName = msg.name)
                is Message.LoadingUpdated -> copy(isLoading = msg.isLoading)
                is Message.RecipeDataUpdated -> copy(recipeData = msg.data)
                is Message.ErrorUpdated -> copy(errorMessage = msg.error)
                is Message.TabSelected -> copy(selectedTab = msg.tab)
            }
    }
}