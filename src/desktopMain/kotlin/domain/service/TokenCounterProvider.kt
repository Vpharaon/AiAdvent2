package domain.service

/**
 * Desktop-специфичная реализация провайдера TokenCounter.
 * Возвращает реализацию на основе jtokkit.
 */
actual fun getTokenCounter(): ITokenCounter? {
    return try {
        TokenCounter()
    } catch (e: Exception) {
        // Если не удалось инициализировать TokenCounter, возвращаем null
        println("Не удалось инициализировать TokenCounter: ${e.message}")
        null
    }
}