package data.network.model

import data.network.model.mcp.McpTool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Конвертер для преобразования MCP инструментов в формат LLM tools
 */
object McpToolConverter {
    /**
     * Преобразует список MCP инструментов в список LLM tools
     */
    fun convertMcpToolsToLlmTools(mcpTools: List<McpTool>): List<Tool> {
        return mcpTools.map { mcpTool ->
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = mcpTool.name,
                    description = mcpTool.description,
                    parameters = mcpTool.inputSchema
                )
            )
        }
    }

    /**
     * Создает список погодных инструментов для использования с LLM
     */
    fun createWeatherTools(): List<Tool> {
        return listOf(
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_current_weather",
                    description = "Get current weather for a specified city. Returns temperature, conditions, humidity, wind speed, and pressure.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("city", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("City name in English (e.g., 'London', 'Moscow', 'New York')"))
                            })
                            put("units", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Temperature units"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("metric"))
                                    add(JsonPrimitive("imperial"))
                                    add(JsonPrimitive("standard"))
                                })
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("city"))
                        })
                    }
                )
            ),
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_weather_forecast",
                    description = "Get 5-day weather forecast for a specified city. Returns detailed forecast with temperatures and conditions for the next 5 days.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("city", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("City name in English (e.g., 'London', 'Moscow', 'New York')"))
                            })
                            put("units", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Temperature units"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("metric"))
                                    add(JsonPrimitive("imperial"))
                                    add(JsonPrimitive("standard"))
                                })
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("city"))
                        })
                    }
                )
            )
        )
    }

    /**
     * Создает список инструментов для работы со временем
     */
    fun createTimeTools(): List<Tool> {
        return listOf(
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_city_time",
                    description = "Get current time, date, and timezone information for a specified city. Returns current time, date, day of week, timezone, UTC offset, DST status, and Unix timestamp.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("city", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("City name in English (e.g., 'London', 'Moscow', 'Tokyo')"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("city"))
                        })
                    }
                )
            )
        )
    }
}