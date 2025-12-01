package data.network

import data.network.model.* // Убедитесь, что импортируете все ваши data-классы
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class LLMApiClient(
    private val apiKey: String,
    private val apiUrl: String = "https://api.z.ai/api/paas/v4/chat/completions",
    private val model: String = "glm-4.6"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        // !!! ВОТ КЛЮЧЕВОЙ МОМЕНТ !!!
        install(HttpTimeout) {
            // Максимальное время ожидания начала ответа от сервера
            requestTimeoutMillis = 120_000L // 120 секунд

            // Максимальное время на установку соединения (connect)
            connectTimeoutMillis = 30_000L // 30 секунд

            // Максимальное время бездействия между пакетами данных (socket)
            socketTimeoutMillis = 120_000L // 120 секунд
        }

        /*install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }*/
    }

    /**
     * Отправляет сообщение на LLM API и возвращает полный ответ от сервера.
     *
     * @param messages Список сообщений, составляющих диалог.
     * @return [Result], содержащий либо полный [ChatResponse] в случае успеха,
     *         либо [Exception] в случае ошибки.
     */
    // 1. Изменен тип возвращаемого значения на Result<ChatResponse>
    suspend fun sendMessage(messages: List<ChatMessage>): Result<ChatResponse> {
        return try {
            val request = ChatRequest(
                model = model,
                messages = messages
                // Здесь можно добавить и другие параметры из ChatRequest, например:
                // temperature = 0.7,
                // max_tokens = 1024
            )

            // Ktor автоматически десериализует JSON-ответ в ваш data-класс ChatResponse
            val response: ChatResponse = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }.body()

            // 2. Возвращаем успешный результат с полным объектом ответа
            Result.success(response)

        } catch (e: Exception) {
            // 3. Возвращаем результат с ошибкой
            // Это стандартный и идиоматичный способ обработки ошибок в таких функциях.
            e.printStackTrace() // Рекомендуется для логирования ошибок при отладке
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}