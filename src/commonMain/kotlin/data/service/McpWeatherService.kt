package data.service

import data.network.McpApiClient
import data.network.model.mcp.WeatherToolRequestBuilder
import domain.ApiError

/**
 * Сервис для работы с погодой через MCP сервер
 */
class McpWeatherService(
    private val mcpClient: McpApiClient = McpApiClient()
) {
    /**
     * Получить текущую погоду для города
     *
     * @param city Название города (например, "London", "Moscow", "New York")
     * @param units Единицы измерения ("metric" - Цельсий, "imperial" - Фаренгейт, "standard" - Кельвин)
     * @return Текстовое описание погоды или ошибка
     */
    suspend fun getCurrentWeather(city: String, units: String = "metric"): Result<String> {
        return try {
            val request = WeatherToolRequestBuilder.getCurrentWeather(city, units)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in weather response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get weather", cause = e))
        }
    }

    /**
     * Получить 5-дневный прогноз погоды для города
     *
     * @param city Название города
     * @param units Единицы измерения
     * @return Текстовое описание прогноза или ошибка
     */
    suspend fun getWeatherForecast(city: String, units: String = "metric"): Result<String> {
        return try {
            val request = WeatherToolRequestBuilder.getWeatherForecast(city, units)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in forecast response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get forecast", cause = e))
        }
    }

    /**
     * Получить текущее время, дату и информацию о часовом поясе для города
     *
     * @param city Название города (например, "London", "Moscow", "Tokyo")
     * @return Текстовое описание времени или ошибка
     */
    suspend fun getCityTime(city: String): Result<String> {
        return try {
            val request = WeatherToolRequestBuilder.getCityTime(city)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in time response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get city time", cause = e))
        }
    }

    /**
     * Закрыть клиент
     */
    fun close() {
        mcpClient.close()
    }
}