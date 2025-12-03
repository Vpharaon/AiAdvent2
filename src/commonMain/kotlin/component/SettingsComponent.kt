package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import data.repository.SettingsRepository
import domain.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import mvi.settings.SettingsStore
import mvi.settings.SettingsStoreFactory

interface SettingsComponent {
    val state: StateFlow<SettingsStore.State>

    fun onThemeChange(theme: Theme)
    fun onTemperatureChange(temperature: Double)
    fun onMaxTokensChange(maxTokens: Int?)
    fun onResetClick()
    fun onBackClick()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    settingsRepository: SettingsRepository,
    private val onNavigateBack: () -> Unit
) : SettingsComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        SettingsStoreFactory(
            storeFactory = storeFactory,
            settingsRepository = settingsRepository
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<SettingsStore.State> = store.stateFlow

    override fun onThemeChange(theme: Theme) {
        store.accept(SettingsStore.Intent.UpdateTheme(theme))
    }

    override fun onTemperatureChange(temperature: Double) {
        store.accept(SettingsStore.Intent.UpdateTemperature(temperature))
    }

    override fun onMaxTokensChange(maxTokens: Int?) {
        store.accept(SettingsStore.Intent.UpdateMaxTokens(maxTokens))
    }

    override fun onResetClick() {
        store.accept(SettingsStore.Intent.ResetToDefaults)
    }

    override fun onBackClick() {
        onNavigateBack()
    }
}