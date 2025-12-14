package di

import data.source.local.ChatLocalDataSource
import data.source.local.ChatLocalDataSourceImpl
import domain.service.TokenCounter
import org.koin.dsl.module

/**
 * Desktop-специфичный модуль внедрения зависимостей.
 * Содержит зависимости, которые доступны только на Desktop платформе.
 */
val desktopModule = module {
    // Services - Desktop only
    single {
        TokenCounter()
    }

    // Data Sources - Desktop only
    single<ChatLocalDataSource> {
        ChatLocalDataSourceImpl()
    }
}