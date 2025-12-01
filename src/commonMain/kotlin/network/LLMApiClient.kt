package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import model.LLMResponse

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val max_tokens: Int = 500
)

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>
)

@Serializable
data class OpenAIChoice(
    val message: OpenAIMessage
)

class LLMApiClient(
    private val apiKey: String,
    private val apiUrl: String = "https://api.openai.com/v1/chat/completions",
    private val model: String = "gpt-3.5-turbo"
) {
    // Настроенный Ktor Client
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    // Функция для отправки сообщения на LLM API
    suspend fun sendMessage(userMessage: String): LLMResponse {
        return try {
            // Подготовка запроса
            val request = OpenAIRequest(
                model = model,
                messages = listOf(
                    OpenAIMessage(role = "user", content = userMessage)
                )
            )

            // Отправка запроса
            val response: OpenAIResponse = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }.body()

            // Обработка ответа
            val botMessage = response.choices.firstOrNull()?.message?.content
                ?: "Нет ответа от LLM"

            LLMResponse(
                message = botMessage,
                success = true
            )
        } catch (e: Exception) {
            // Обработка ошибок сети
            LLMResponse(
                message = "Ошибка при обращении к API: ${e.message}",
                success = false
            )
        }
    }

    // Закрытие клиента
    fun close() {
        client.close()
    }
}