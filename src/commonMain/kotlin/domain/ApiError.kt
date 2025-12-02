package domain

/**
 * Sealed class для представления различных типов ошибок API.
 */
sealed class ApiError(override val message: String) : Exception(message) {
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