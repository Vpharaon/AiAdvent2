package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import data.network.LLMApi
import data.repository.ChatRepository
import data.repository.SettingsRepository
import domain.usecase.chat.ClearChatUseCase
import domain.usecase.chat.SendMessageUseCase
import domain.usecase.chat.SendSystemPromptUseCase
import domain.usecase.recipe.GetRecipeUseCase
import domain.usecase.settings.ResetSettingsUseCase
import domain.usecase.settings.UpdateMaxTokensUseCase
import domain.usecase.settings.UpdateTemperatureUseCase
import domain.usecase.settings.UpdateThemeUseCase
import kotlinx.serialization.Serializable

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    fun navigateToChat()
    fun navigateToRecipes()
    fun navigateToEventPlanner()
    fun navigateToSettings()
    fun navigateBack()

    sealed class Child {
        data class Home(val component: HomeComponent) : Child()
        data class Chat(val component: ChatComponent) : Child()
        data class Recipe(val component: RecipeComponent) : Child()
        data class EventPlanner(val component: EventPlannerComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val storeFactory: StoreFactory,
    private val llmApi: LLMApi,
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
    // Chat Use Cases
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendSystemPromptUseCase: SendSystemPromptUseCase,
    private val clearChatUseCase: ClearChatUseCase,
    // Recipe Use Cases
    private val getRecipeUseCase: GetRecipeUseCase,
    // Settings Use Cases
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateTemperatureUseCase: UpdateTemperatureUseCase,
    private val updateMaxTokensUseCase: UpdateMaxTokensUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.Home -> RootComponent.Child.Home(
                DefaultHomeComponent(
                    componentContext = componentContext,
                    onNavigateToChat = ::navigateToChat,
                    onNavigateToRecipes = ::navigateToRecipes,
                    onNavigateToEventPlanner = ::navigateToEventPlanner
                )
            )
            is Config.Chat -> RootComponent.Child.Chat(
                DefaultChatComponent(
                    componentContext = componentContext,
                    storeFactory = storeFactory,
                    chatRepository = chatRepository,
                    sendMessageUseCase = sendMessageUseCase,
                    sendSystemPromptUseCase = sendSystemPromptUseCase,
                    clearChatUseCase = clearChatUseCase,
                    onNavigateBack = ::navigateBack,
                    onNavigateToSettings = ::navigateToSettings
                )
            )
            is Config.Recipe -> RootComponent.Child.Recipe(
                DefaultRecipeComponent(
                    componentContext = componentContext,
                    storeFactory = storeFactory,
                    getRecipeUseCase = getRecipeUseCase,
                    onNavigateBack = ::navigateBack
                )
            )
            is Config.EventPlanner -> RootComponent.Child.EventPlanner(
                DefaultEventPlannerComponent(
                    componentContext = componentContext,
                    storeFactory = storeFactory,
                    llmApi = llmApi,
                    settingsRepository = settingsRepository,
                    onNavigateBack = ::navigateBack
                )
            )
            is Config.Settings -> RootComponent.Child.Settings(
                DefaultSettingsComponent(
                    componentContext = componentContext,
                    storeFactory = storeFactory,
                    settingsRepository = settingsRepository,
                    updateThemeUseCase = updateThemeUseCase,
                    updateTemperatureUseCase = updateTemperatureUseCase,
                    updateMaxTokensUseCase = updateMaxTokensUseCase,
                    resetSettingsUseCase = resetSettingsUseCase,
                    onNavigateBack = ::navigateBack
                )
            )
        }

    override fun navigateToChat() {
        navigation.push(Config.Chat)
    }

    override fun navigateToRecipes() {
        navigation.push(Config.Recipe)
    }

    override fun navigateToEventPlanner() {
        navigation.push(Config.EventPlanner)
    }

    override fun navigateToSettings() {
        navigation.push(Config.Settings)
    }

    override fun navigateBack() {
        navigation.pop()
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Home : Config

        @Serializable
        data object Chat : Config

        @Serializable
        data object Recipe : Config

        @Serializable
        data object EventPlanner : Config

        @Serializable
        data object Settings : Config
    }
}