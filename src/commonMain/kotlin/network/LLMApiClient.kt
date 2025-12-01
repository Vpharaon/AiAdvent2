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
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val max_tokens: Int = 500
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice>
)

@Serializable
data class ChatChoice(
    val message: ChatMessage
)

class LLMApiClient(
    private val apiKey: String,
    private val apiUrl: String = "https://open.bigmodel.cn/api/paas/v4/chat/completions",
    private val model: String = "glm-4-flash"
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
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage(role = "user", content = userMessage)
                )
            )

            // Отправка запроса
            val response: ChatResponse = client.post(apiUrl) {
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