package mvi.settings

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.SettingsRepository
import domain.AppSettings
import domain.usecase.settings.ResetSettingsUseCase
import domain.usecase.settings.UpdateMaxTokensUseCase
import domain.usecase.settings.UpdateTemperatureUseCase
import domain.usecase.settings.UpdateThemeUseCase
import kotlinx.coroutines.launch

/**
 * Factory для создания SettingsStore.
 * Использует Use Cases для выполнения бизнес-логики.
 */
internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val settingsRepository: SettingsRepository,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateTemperatureUseCase: UpdateTemperatureUseCase,
    private val updateMaxTokensUseCase: UpdateMaxTokensUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase
) {

    private sealed interface Action {
        data object InitAction : Action
    }

    fun create(): SettingsStore =
        object : SettingsStore, Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
            bootstrapper = SimpleBootstrapper(
                Action.InitAction
            )
        ) {}

    private sealed interface Message {
        data class SettingsUpdated(val settings: AppSettings) : Message
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Action, SettingsStore.State, Message, Nothing>() {

        override fun executeAction(action: Action) {
            super.executeAction(action)
            when (action) {
                Action.InitAction -> {
                    // Подписка на изменения настроек
                    scope.launch {
                        settingsRepository.settings.collect { settings ->
                            dispatch(Message.SettingsUpdated(settings))
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: SettingsStore.Intent) {
            when (intent) {
                is SettingsStore.Intent.UpdateTheme -> {
                    updateThemeUseCase(intent.theme)
                }

                is SettingsStore.Intent.UpdateTemperature -> {
                    updateTemperatureUseCase(intent.temperature)
                }

                is SettingsStore.Intent.UpdateMaxTokens -> {
                    updateMaxTokensUseCase(intent.maxTokens)
                }

                is SettingsStore.Intent.ResetToDefaults -> {
                    resetSettingsUseCase()
                }
            }
        }
    }

    private object ReducerImpl : Reducer<SettingsStore.State, Message> {
        override fun SettingsStore.State.reduce(msg: Message): SettingsStore.State =
            when (msg) {
                is Message.SettingsUpdated -> copy(settings = msg.settings)
            }
    }
}