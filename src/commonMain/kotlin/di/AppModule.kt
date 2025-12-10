package di

import data.network.LLMApi
import data.network.LLMApiClient
import data.repository.ChatRepository
import data.repository.ChatRepositoryImpl
import data.repository.SettingsRepository
import data.repository.SettingsRepositoryImpl
import data.source.remote.LLMRemoteDataSource
import data.source.remote.LLMRemoteDataSourceImpl
import domain.usecase.chat.ClearChatUseCase
import domain.usecase.chat.SendMessageUseCase
import domain.usecase.chat.SendSystemPromptUseCase
import domain.usecase.settings.ResetSettingsUseCase
import domain.usecase.settings.UpdateMaxTokensUseCase
import domain.usecase.settings.UpdateTemperatureUseCase
import domain.usecase.settings.UpdateThemeUseCase
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

/**
 * Модуль внедрения зависимостей приложения.
 * Следует принципу Dependency Inversion (SOLID).
 *
 * @param apiKeys Карта с API ключами для различных провайдеров
 * @param coroutineScope Область видимости корутин для асинхронных операций
 */
fun appModule(apiKeys: Map<String, String>, coroutineScope: CoroutineScope) = module {

    // Data Layer - Network
    single<LLMApi> {
        LLMApiClient(apiKeys = apiKeys)
    }

    // Data Layer - Data Sources
    single<LLMRemoteDataSource> {
        LLMRemoteDataSourceImpl(llmApi = get())
    }

    // Data Layer - Repositories
    single<SettingsRepository> {
        SettingsRepositoryImpl()
    }

    single<ChatRepository> {
        ChatRepositoryImpl(
            remoteDataSource = get(),
            settingsRepository = get()
        )
    }

    // Domain Layer - Chat Use Cases
    factory {
        SendMessageUseCase(chatRepository = get())
    }

    factory {
        SendSystemPromptUseCase(chatRepository = get())
    }

    factory {
        ClearChatUseCase(chatRepository = get())
    }

    // Domain Layer - Settings Use Cases
    factory {
        UpdateThemeUseCase(settingsRepository = get())
    }

    factory {
        UpdateTemperatureUseCase(settingsRepository = get())
    }

    factory {
        UpdateMaxTokensUseCase(settingsRepository = get())
    }

    factory {
        ResetSettingsUseCase(settingsRepository = get())
    }

    // Domain Layer - Services
    // Note: TokenCounter is desktop-only, will be provided separately
}