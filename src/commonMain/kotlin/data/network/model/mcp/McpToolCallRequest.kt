package data.network.model.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Запрос для вызова MCP инструмента
 */
@Serializable
data class McpToolCallRequest(
    val jsonrpc: String = "2.0",
    val id: String,
    val method: String = "tools/call",
    val params: ToolCallParams
)

@Serializable
data class ToolCallParams(
    val name: String,
    val arguments: JsonObject
)

/**
 * Вспомогательный класс для создания запросов на получение погоды
 */
object WeatherToolRequestBuilder {
    fun getCurrentWeather(city: String, units: String = "metric"): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_current_weather",
                arguments = buildJsonObject {
                    put("city", city)
                    put("units", units)
                }
            )
        )
    }

    fun getWeatherForecast(city: String, units: String = "metric"): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_weather_forecast",
                arguments = buildJsonObject {
                    put("city", city)
                    put("units", units)
                }
            )
        )
    }

    fun getCityTime(city: String): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_city_time",
                arguments = buildJsonObject {
                    put("city", city)
                }
            )
        )
    }
}