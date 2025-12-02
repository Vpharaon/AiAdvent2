package di

import kotlinx.coroutines.CoroutineScope
import data.network.LLMApi
import data.network.LLMApiClient
import org.koin.dsl.module
import data.repository.ChatRepository
import data.repository.SettingsRepository
import data.repository.StructuredChatRepository
import viewmodel.ChatViewModel

fun appModule(apiKey: String, coroutineScope: CoroutineScope) = module {
    // Settings Repository
    single { SettingsRepository() }

    // LLM API Client
    single<LLMApi> { LLMApiClient(apiKey = apiKey) }

    // Chat Repository
    single {
        ChatRepository(
            llmApiClient = get(),
            settingsRepository = get()
        )
    }

    // Structured Chat Repository
    single {
        StructuredChatRepository(
            llmApiClient = get(),
            settingsRepository = get()
        )
    }

    // Chat ViewModel
    single {
        ChatViewModel(
            repository = get(),
            coroutineScope = coroutineScope
        )
    }
}