package di

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
}