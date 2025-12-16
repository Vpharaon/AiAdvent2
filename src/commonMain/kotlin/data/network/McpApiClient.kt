package data.network

import data.network.model.mcp.*
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

/**
 * Клиент для работы с MCP (Model Context Protocol) сервером
 */
class McpApiClient(
    private val mcpUrl: String = "http://138.124.108.251:8080/mcp"
) {
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
            requestTimeoutMillis = 60_000L // 60 секунд
            connectTimeoutMillis = 30_000L // 30 секунд
            socketTimeoutMillis = 60_000L // 60 секунд
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
    }

    /**
     * Инициализация соединения с MCP сервером
     */
    suspend fun initialize(): Result<McpResponse> {
        return try {
            val request = McpRequest(
                id = System.currentTimeMillis().toString(),
                method = "initialize",
                params = null
            )

            val httpResponse = client.post(mcpUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            when (httpResponse.status.value) {
                in 200..299 -> {
                    val response: McpResponse = httpResponse.body()
                    Result.success(response)
                }
                else -> Result.failure(
                    ApiError.UnknownError(
                        message = "MCP initialization failed with status: ${httpResponse.status.value}"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(handleException(e))
        }
    }

    /**
     * Получить список доступных инструментов
     */
    suspend fun getTools(): Result<McpToolsResponse> {
        return try {
            val request = McpRequest(
                id = System.currentTimeMillis().toString(),
                method = "tools/list",
                params = null
            )

            val httpResponse = client.post(mcpUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            when (httpResponse.status.value) {
                in 200..299 -> {
                    val response: McpToolsResponse = httpResponse.body()
                    Result.success(response)
                }
                else -> Result.failure(
                    ApiError.UnknownError(
                        message = "Failed to get tools with status: ${httpResponse.status.value}"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(handleException(e))
        }
    }

    /**
     * Вызвать MCP инструмент
     */
    suspend fun callTool(request: McpToolCallRequest): Result<McpToolCallResponse> {
        return try {
            val httpResponse = client.post(mcpUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            when (httpResponse.status.value) {
                in 200..299 -> {
                    val response: McpToolCallResponse = httpResponse.body()
                    if (response.error != null) {
                        Result.failure(
                            ApiError.ServerError(
                                code = response.error.code,
                                message = response.error.message
                            )
                        )
                    } else {
                        Result.success(response)
                    }
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
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(handleException(e))
        }
    }

    private fun handleException(e: Exception): ApiError {
        return when (e) {
            is SocketTimeoutException -> ApiError.TimeoutError(message = "Request timeout: ${e.message}")
            is UnknownHostException -> ApiError.NetworkError(message = "Unknown host: ${e.message}", cause = e)
            is UnresolvedAddressException -> ApiError.NetworkError(
                message = "Cannot resolve address: ${e.message}",
                cause = e
            )
            is SerializationException -> ApiError.ParseError(
                message = "Failed to parse response: ${e.message}",
                cause = e
            )
            else -> ApiError.UnknownError(message = e.message ?: "Unknown error", cause = e)
        }
    }

    fun close() {
        client.close()
    }
}