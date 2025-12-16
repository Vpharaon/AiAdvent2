package data.network

import data.network.model.*
import domain.ApiError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.network.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LLMApiClient(
    private val apiKeys: Map<String, String>
) : LLMApi {

    /**
     * Определяет правильный API ключ на основе URL используя LlmProvider
     */
    private fun getApiKeyForUrl(apiUrl: String): String {
        val provider = domain.LlmProvider.fromUrl(apiUrl)
        return provider?.let { apiKeys[it.apiKeyName] } ?: apiKeys["glm"] ?: ""
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true  // Включаем сериализацию полей со значениями по умолчанию
            })
        }

        install(HttpTimeout) {
            // Максимальное время ожидания начала ответа от сервера
            requestTimeoutMillis = 120_000L // 120 секунд

            // Максимальное время на установку соединения (connect)
            connectTimeoutMillis = 30_000L // 30 секунд

            // Максимальное время бездействия между пакетами данных (socket)
            socketTimeoutMillis = 120_000L // 120 секунд
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
    }

    override suspend fun sendMessage(
        messages: List<ChatMessage>,
        temperature: Double?,
        maxTokens: Int?,
        apiUrl: String,
        modelName: String,
        tools: List<Tool>?
    ): Result<ChatResponse> {
        return try {
            val request = ChatRequest(
                model = modelName,
                messages = messages,
                temperature = temperature,
                max_tokens = maxTokens,
                tools = tools
            )

            // Получаем правильный API ключ для данного URL
            val selectedApiKey = getApiKeyForUrl(apiUrl)

            val httpResponse = client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $selectedApiKey")
                setBody(request)
            }

            // Проверяем статус ответа
            when (httpResponse.status.value) {
                in 200..299 -> {
                    val response: ChatResponse = httpResponse.body()
                    Result.success(response)
                }
                in 400..499 -> {
                    val errorBody = try {
                        httpResponse.body<String>()
                    } catch (e: Exception) {
                        "Client error"
                    }
                    Result.failure(
                        ApiError.ClientError(
                            code = httpResponse.status.value,
                            message = errorBody
                        )
                    )
                }
                in 500..599 -> {
                    val errorBody = try {
                        httpResponse.body<String>()
                    } catch (e: Exception) {
                        "Server error"
                    }
                    Result.failure(
                        ApiError.ServerError(
                            code = httpResponse.status.value,
                            message = errorBody
                        )
                    )
                }
                else -> {
                    Result.failure(
                        ApiError.UnknownError(
                            message = "Unexpected status code: ${httpResponse.status.value}"
                        )
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            Result.failure(ApiError.TimeoutError(message = "Request timeout: ${e.message}"))
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            Result.failure(ApiError.NetworkError(message = "Unknown host: ${e.message}", cause = e))
        } catch (e: UnresolvedAddressException) {
            e.printStackTrace()
            Result.failure(ApiError.NetworkError(message = "Cannot resolve address: ${e.message}", cause = e))
        } catch (e: SerializationException) {
            e.printStackTrace()
            Result.failure(ApiError.ParseError(message = "Failed to parse response: ${e.message}", cause = e))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Unknown error", cause = e))
        }
    }

    override fun close() {
        client.close()
    }
}