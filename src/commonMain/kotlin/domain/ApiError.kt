package domain

/**
 * Sealed class для представления различных типов ошибок API.
 *
 * Предоставляет типизированную иерархию ошибок для обработки различных сценариев
 * сбоев при взаимодействии с LLM API. Каждый тип ошибки имеет понятное пользователю
 * сообщение через метод [getUserFriendlyMessage].
 *
 * Типы ошибок:
 * - [NetworkError]: Проблемы с сетевым подключением
 * - [TimeoutError]: Превышено время ожидания ответа
 * - [ServerError]: Ошибки на стороне сервера (5xx)
 * - [ClientError]: Ошибки запроса (4xx) - неверный API ключ, лимиты и т.д.
 * - [ParseError]: Ошибки парсинга ответа от сервера
 * - [UnknownError]: Неизвестные или непредвиденные ошибки
 *
 * @sample
 * ```kotlin
 * when (error) {
 *     is ApiError.ClientError -> if (error.code == 401) {
 *         println("Проверьте API ключ")
 *     }
 *     is ApiError.NetworkError -> println("Проверьте интернет соединение")
 *     else -> println(error.getUserFriendlyMessage())
 * }
 * ```
 */
sealed class ApiError(override val message: String) : Exception(message) {
    /**
     * Возвращает понятное пользователю сообщение об ошибке.
     *
     * @return Локализованное сообщение, которое можно показать в UI
     */
    abstract fun getUserFriendlyMessage(): String

    /**
     * Ошибка сети (нет подключения, таймаут и т.д.)
     */
    class NetworkError(
        message: String,
        cause: Throwable? = null
    ) : ApiError(message) {
        init {
            initCause(cause)
        }
        override fun getUserFriendlyMessage() = "Ошибка сети: проверьте подключение к интернету"
    }

    /**
     * Превышено время ожидания ответа от сервера
     */
    class TimeoutError(
        message: String
    ) : ApiError(message) {
        override fun getUserFriendlyMessage() = "Превышено время ожидания ответа от сервера"
    }

    /**
     * Ошибка на стороне сервера (5xx)
     */
    class ServerError(
        val code: Int,
        message: String
    ) : ApiError(message) {
        override fun getUserFriendlyMessage() = "Ошибка сервера ($code): попробуйте позже"
    }

    /**
     * Ошибка клиента (4xx)
     */
    class ClientError(
        val code: Int,
        message: String
    ) : ApiError(message) {
        override fun getUserFriendlyMessage() = when (code) {
            400 -> "Некорректный запрос"
            401 -> "Ошибка авторизации: проверьте API ключ"
            403 -> "Доступ запрещен"
            404 -> "Ресурс не найден"
            429 -> "Превышен лимит запросов: попробуйте позже"
            else -> "Ошибка запроса ($code)"
        }
    }

    /**
     * Ошибка парсинга ответа
     */
    class ParseError(
        message: String,
        cause: Throwable? = null
    ) : ApiError(message) {
        init {
            initCause(cause)
        }
        override fun getUserFriendlyMessage() = "Ошибка обработки ответа от сервера"
    }

    /**
     * Неизвестная ошибка
     */
    class UnknownError(
        message: String,
        cause: Throwable? = null
    ) : ApiError(message) {
        init {
            initCause(cause)
        }
        override fun getUserFriendlyMessage() = "Произошла неизвестная ошибка: $message"
    }
}