package di

import kotlinx.coroutines.CoroutineScope
import data.network.LLMApiClient
import org.koin.dsl.module
import data.repository.ChatRepository
import viewmodel.ChatViewModel

fun appModule(apiKey: String, coroutineScope: CoroutineScope) = module {
    // LLM API Client
    single { LLMApiClient(apiKey = apiKey) }

    // Chat Repository
    single {
        ChatRepository(
            llmApiClient = { message ->
                get<LLMApiClient>().sendMessage(message)
            }
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