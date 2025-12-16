package data.repository

import data.network.model.ChatMessage
import data.network.model.MessageRole
import data.network.model.ToolCall
import data.service.McpWeatherService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Обработчик вызовов инструментов (tool calls) от LLM
 */
class ToolCallHandler(
    private val weatherService: McpWeatherService
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Обрабатывает вызовы инструментов и создает сообщения с результатами
     *
     * @param toolCalls Список вызовов инструментов от LLM
     * @return Список сообщений с результатами выполнения инструментов
     */
    suspend fun handleToolCalls(toolCalls: List<ToolCall>): List<ChatMessage> {
        return toolCalls.map { toolCall ->
            val result = executeToolCall(toolCall)
            ChatMessage(
                role = MessageRole.TOOL,
                content = result,
                toolCallId = toolCall.id,
                name = toolCall.function.name
            )
        }
    }

    /**
     * Выполняет вызов конкретного инструмента
     */
    private suspend fun executeToolCall(toolCall: ToolCall): String {
        return try {
            when (toolCall.function.name) {
                "get_current_weather" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val city = args["city"]?.jsonPrimitive?.content
                        ?: return "Error: City parameter is required"
                    val units = args["units"]?.jsonPrimitive?.content ?: "metric"

                    val result = weatherService.getCurrentWeather(city, units)
                    result.getOrElse { error ->
                        "Error getting weather: ${error.message}"
                    }
                }

                "get_weather_forecast" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val city = args["city"]?.jsonPrimitive?.content
                        ?: return "Error: City parameter is required"
                    val units = args["units"]?.jsonPrimitive?.content ?: "metric"

                    val result = weatherService.getWeatherForecast(city, units)
                    result.getOrElse { error ->
                        "Error getting forecast: ${error.message}"
                    }
                }

                "get_city_time" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val city = args["city"]?.jsonPrimitive?.content
                        ?: return "Error: City parameter is required"

                    val result = weatherService.getCityTime(city)
                    result.getOrElse { error ->
                        "Error getting city time: ${error.message}"
                    }
                }

                else -> "Error: Unknown tool '${toolCall.function.name}'"
            }
        } catch (e: Exception) {
            "Error executing tool '${toolCall.function.name}': ${e.message}"
        }
    }
}