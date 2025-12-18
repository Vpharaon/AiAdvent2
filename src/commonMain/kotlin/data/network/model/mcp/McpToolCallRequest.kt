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

/**
 * Вспомогательный класс для создания запросов на работу с задачами
 */
object ReminderToolRequestBuilder {
    /**
     * Создать новую задачу
     */
    fun addReminder(
        title: String? = null,
        description: String,
        reminderTime: String,
        recurrence: String? = null,
        importance: String = "MEDIUM"
    ): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "add_task",
                arguments = buildJsonObject {
                    title?.let { put("title", it) }
                    put("description", description)
                    put("reminder_time", reminderTime)
                    recurrence?.let { put("recurrence", it) }
                    put("importance", importance)
                }
            )
        )
    }

    /**
     * Получить список задач
     */
    fun listReminders(status: String? = null): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "list_tasks",
                arguments = buildJsonObject {
                    status?.let { put("status", it) }
                }
            )
        )
    }

    /**
     * Получить конкретную задачу по ID
     */
    fun getReminder(id: Int): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_task",
                arguments = buildJsonObject {
                    put("id", id)
                }
            )
        )
    }

    /**
     * Пометить задачу как выполненную
     */
    fun completeReminder(id: Int): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "complete_task",
                arguments = buildJsonObject {
                    put("id", id)
                }
            )
        )
    }

    /**
     * Удалить задачу
     */
    fun deleteReminder(id: Int): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "delete_task",
                arguments = buildJsonObject {
                    put("id", id)
                }
            )
        )
    }

    /**
     * Получить сводку по всем задачам
     */
    fun getRemindersSummary(): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_tasks_summary",
                arguments = buildJsonObject {}
            )
        )
    }

    /**
     * Получить все задачи на конкретную дату
     */
    fun getTasksForDate(date: String): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_tasks_for_date",
                arguments = buildJsonObject {
                    put("date", date)
                }
            )
        )
    }

    /**
     * Получить задачи отфильтрованные по уровню важности
     */
    fun getTasksByImportance(importance: String): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_tasks_by_importance",
                arguments = buildJsonObject {
                    put("importance", importance)
                }
            )
        )
    }

    /**
     * Настроить расписание уведомлений
     */
    fun setNotificationSchedule(intervalMinutes: Int, enabled: Boolean = true): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "set_notification_schedule",
                arguments = buildJsonObject {
                    put("interval_minutes", intervalMinutes)
                    put("enabled", enabled)
                }
            )
        )
    }

    /**
     * Получить текущие настройки расписания уведомлений
     */
    fun getNotificationSchedule(): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_notification_schedule",
                arguments = buildJsonObject {}
            )
        )
    }

    /**
     * Отправить тестовое уведомление
     */
    fun sendTestNotification(): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "send_test_notification",
                arguments = buildJsonObject {}
            )
        )
    }
}