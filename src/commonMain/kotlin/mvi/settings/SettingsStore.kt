package mvi.settings

import com.arkivanov.mvikotlin.core.store.Store
import domain.AppSettings
import domain.Theme
import mvi.settings.SettingsStore.Intent
import mvi.settings.SettingsStore.State

interface SettingsStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class UpdateTheme(val theme: Theme) : Intent
        data class UpdateTemperature(val temperature: Double) : Intent
        data class UpdateMaxTokens(val maxTokens: Int?) : Intent
        data object ResetToDefaults : Intent
    }

    data class State(
        val settings: AppSettings = AppSettings()
    )
}