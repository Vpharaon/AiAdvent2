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

    /**
     * Создает список инструментов для работы с напоминаниями
     */
    fun createReminderTools(): List<Tool> {
        return listOf(
            // Создать напоминание
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "add_reminder",
                    description = "Create a new reminder/task with title, description, optional due date and priority. Use this when user wants to remember something or create a task.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("title", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Short title of the task"))
                            })
                            put("description", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Detailed description of the task"))
                            })
                            put("due_date", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Due date in ISO format (2024-12-17T15:30:00)"))
                            })
                            put("priority", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Task priority"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("LOW"))
                                    add(JsonPrimitive("MEDIUM"))
                                    add(JsonPrimitive("HIGH"))
                                    add(JsonPrimitive("URGENT"))
                                })
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("title"))
                            add(JsonPrimitive("description"))
                        })
                    }
                )
            ),
            // Список напоминаний
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "list_reminders",
                    description = "Get list of all reminders or filter by status. Use this when user wants to see their tasks or reminders.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("status", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Filter by status"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("ACTIVE"))
                                    add(JsonPrimitive("COMPLETED"))
                                    add(JsonPrimitive("ARCHIVED"))
                                })
                            })
                        })
                        put("required", buildJsonArray {})
                    }
                )
            ),
            // Получить конкретное напоминание
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_reminder",
                    description = "Get detailed information about a specific reminder by ID.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Reminder ID"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                        })
                    }
                )
            ),
            // Отметить как выполненное
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "complete_reminder",
                    description = "Mark a reminder as completed. Use this when user finishes a task.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Reminder ID"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                        })
                    }
                )
            ),
            // Удалить напоминание
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "delete_reminder",
                    description = "Delete a reminder permanently. Use this when user wants to remove a task.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Reminder ID"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                        })
                    }
                )
            ),
            // Сводка по напоминаниям
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_reminders_summary",
                    description = "Get summary of all reminders including statistics, overdue, high-priority, and upcoming tasks. Use this when user wants an overview of their tasks.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {})
                        put("required", buildJsonArray {})
                    }
                )
            ),
            // Настроить расписание уведомлений
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "set_notification_schedule",
                    description = "Set up automatic periodic notifications with reminders summary. Use this when user wants to receive regular updates about their tasks.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("interval_minutes", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Interval in minutes (60 = hourly, 1440 = daily)"))
                            })
                            put("enabled", buildJsonObject {
                                put("type", JsonPrimitive("boolean"))
                                put("description", JsonPrimitive("Enable or disable notifications"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("interval_minutes"))
                        })
                    }
                )
            ),
            // Получить расписание уведомлений
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_notification_schedule",
                    description = "Get current notification schedule settings.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {})
                        put("required", buildJsonArray {})
                    }
                )
            ),
            // Отправить тестовое уведомление
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "send_test_notification",
                    description = "Send a test notification immediately to check configuration.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {})
                        put("required", buildJsonArray {})
                    }
                )
            )
        )
    }
}