package di

import kotlinx.coroutines.CoroutineScope
import data.network.LLMApi
import data.network.LLMApiClient
import data.source.remote.LLMRemoteDataSource
import data.source.remote.LLMRemoteDataSourceImpl
import org.koin.dsl.module
import data.repository.ChatRepository
import data.repository.ChatRepositoryImpl
import data.repository.EventPlannerRepository
import data.repository.EventPlannerRepositoryImpl
import data.repository.SettingsRepository
import data.repository.SettingsRepositoryImpl
import data.repository.StructuredChatRepository
import data.repository.StructuredChatRepositoryImpl
import domain.usecase.chat.ClearChatUseCase
import domain.usecase.chat.SendMessageUseCase
import domain.usecase.chat.SendSystemPromptUseCase
import domain.usecase.eventplanner.ClearEventPlanChatUseCase
import domain.usecase.eventplanner.SendEventPlanMessageUseCase
import domain.usecase.eventplanner.StartEventPlanConversationUseCase
import domain.usecase.recipe.GetRecipeUseCase
import domain.usecase.settings.ResetSettingsUseCase
import domain.usecase.settings.UpdateMaxTokensUseCase
import domain.usecase.settings.UpdateModelUseCase
import domain.usecase.settings.UpdateTemperatureUseCase
import domain.usecase.settings.UpdateThemeUseCase

fun appModule(apiKeys: Map<String, String>, coroutineScope: CoroutineScope) = module {
    // Settings Repository
    single<SettingsRepository> { SettingsRepositoryImpl() }

    // LLM API Client
    single<LLMApi> { LLMApiClient(apiKeys = apiKeys) }

    // Data Sources
    single<LLMRemoteDataSource> { LLMRemoteDataSourceImpl(llmApi = get()) }

    // Chat Repository
    single<ChatRepository> {
        ChatRepositoryImpl(
            remoteDataSource = get(),
            settingsRepository = get()
        )
    }

    // Structured Chat Repository
    single<StructuredChatRepository> {
        StructuredChatRepositoryImpl(
            remoteDataSource = get(),
            settingsRepository = get()
        )
    }

    // Event Planner Repository
    single<EventPlannerRepository> {
        EventPlannerRepositoryImpl(
            remoteDataSource = get(),
            settingsRepository = get()
        )
    }

    // Chat Use Cases
    factory { SendMessageUseCase(chatRepository = get()) }
    factory { SendSystemPromptUseCase(chatRepository = get()) }
    factory { ClearChatUseCase(chatRepository = get()) }

    // Recipe Use Cases
    factory { GetRecipeUseCase(structuredChatRepository = get()) }

    // Event Planner Use Cases
    factory { SendEventPlanMessageUseCase(eventPlannerRepository = get()) }
    factory { StartEventPlanConversationUseCase(eventPlannerRepository = get()) }
    factory { ClearEventPlanChatUseCase(eventPlannerRepository = get()) }

    // Settings Use Cases
    factory { UpdateThemeUseCase(settingsRepository = get()) }
    factory { UpdateTemperatureUseCase(settingsRepository = get()) }
    factory { UpdateMaxTokensUseCase(settingsRepository = get()) }
    factory { UpdateModelUseCase(settingsRepository = get()) }
    factory { ResetSettingsUseCase(settingsRepository = get()) }
}