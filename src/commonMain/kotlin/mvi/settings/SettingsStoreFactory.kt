package mvi.settings

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import data.repository.SettingsRepository
import domain.AppSettings
import domain.Theme
import kotlinx.coroutines.launch

internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val settingsRepository: SettingsRepository
) {

    fun create(): SettingsStore =
        object : SettingsStore, Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {
        data class SettingsUpdated(val settings: AppSettings) : Message
    }

    private inner class ExecutorImpl : CoroutineExecutor<SettingsStore.Intent, Nothing, SettingsStore.State, Message, Nothing>() {
        init {
            // Подписка на изменения настроек
            scope.launch {
                settingsRepository.settings.collect { settings ->
                    dispatch(Message.SettingsUpdated(settings))
                }
            }
        }

        override fun executeIntent(intent: SettingsStore.Intent) {
            when (intent) {
                is SettingsStore.Intent.UpdateTheme -> {
                    settingsRepository.updateTheme(intent.theme)
                }

                is SettingsStore.Intent.UpdateTemperature -> {
                    settingsRepository.updateTemperature(intent.temperature)
                }

                is SettingsStore.Intent.UpdateMaxTokens -> {
                    settingsRepository.updateMaxTokens(intent.maxTokens)
                }

                is SettingsStore.Intent.ResetToDefaults -> {
                    settingsRepository.resetToDefaults()
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