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
     * Создает список инструментов для работы с задачами
     */
    fun createReminderTools(): List<Tool> {
        return listOf(
            // Создать задачу
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "add_task",
                    description = "Create a new task with optional title (auto-generated from description if not provided), description, reminder time, optional recurrence, and importance level. Use this when user wants to create a task or reminder.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("title", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Short title of the task (optional, auto-generated if not provided)"))
                            })
                            put("description", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Detailed description of the task"))
                            })
                            put("reminder_time", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Reminder time in ISO format (2024-12-17T15:30:00)"))
                            })
                            put("recurrence", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Recurrence pattern (optional)"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("DAILY"))
                                    add(JsonPrimitive("WEEKLY"))
                                    add(JsonPrimitive("MONTHLY"))
                                })
                            })
                            put("importance", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Task importance level"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("LOW"))
                                    add(JsonPrimitive("MEDIUM"))
                                    add(JsonPrimitive("HIGH"))
                                    add(JsonPrimitive("URGENT"))
                                })
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("description"))
                            add(JsonPrimitive("reminder_time"))
                        })
                    }
                )
            ),
            // Список задач
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "list_tasks",
                    description = "Get list of all tasks or filter by status. Use this when user wants to see their tasks.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("status", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Filter by status"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("ACTIVE"))
                                    add(JsonPrimitive("COMPLETED"))
                                })
                            })
                        })
                        put("required", buildJsonArray {})
                    }
                )
            ),
            // Получить конкретную задачу
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_task",
                    description = "Get detailed information about a specific task by ID.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Task ID"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                        })
                    }
                )
            ),
            // Отметить как выполненную
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "complete_task",
                    description = "Mark a task as completed. Use this when user finishes a task.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Task ID"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                        })
                    }
                )
            ),
            // Удалить задачу
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "delete_task",
                    description = "Delete a task permanently. Use this when user wants to remove a task.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("id", buildJsonObject {
                                put("type", JsonPrimitive("integer"))
                                put("description", JsonPrimitive("Task ID"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("id"))
                        })
                    }
                )
            ),
            // Получить задачи на конкретную дату
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_tasks_for_date",
                    description = "Get all tasks scheduled for a specific date. Use this when user asks about tasks on a particular day.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("date", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Date in ISO format (2024-12-17)"))
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("date"))
                        })
                    }
                )
            ),
            // Получить задачи по важности
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_tasks_by_importance",
                    description = "Get tasks filtered by importance level. Use this when user asks about high-priority or urgent tasks.",
                    parameters = buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put("properties", buildJsonObject {
                            put("importance", buildJsonObject {
                                put("type", JsonPrimitive("string"))
                                put("description", JsonPrimitive("Importance level"))
                                put("enum", buildJsonArray {
                                    add(JsonPrimitive("LOW"))
                                    add(JsonPrimitive("MEDIUM"))
                                    add(JsonPrimitive("HIGH"))
                                    add(JsonPrimitive("URGENT"))
                                })
                            })
                        })
                        put("required", buildJsonArray {
                            add(JsonPrimitive("importance"))
                        })
                    }
                )
            ),
            // Сводка по задачам
            Tool(
                type = "function",
                function = FunctionDefinition(
                    name = "get_tasks_summary",
                    description = "Get summary of all tasks including statistics, overdue, high-priority, and upcoming tasks. Use this when user wants an overview of their tasks.",
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
                    description = "Set up automatic periodic notifications with tasks summary. Use this when user wants to receive regular updates about their tasks.",
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