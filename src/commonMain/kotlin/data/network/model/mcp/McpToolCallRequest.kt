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
 * Вспомогательный класс для создания запросов на работу с напоминаниями
 */
object ReminderToolRequestBuilder {
    /**
     * Создать новое напоминание
     */
    fun addReminder(
        title: String,
        description: String,
        dueDate: String? = null,
        priority: String = "MEDIUM"
    ): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "add_reminder",
                arguments = buildJsonObject {
                    put("title", title)
                    put("description", description)
                    dueDate?.let { put("due_date", it) }
                    put("priority", priority)
                }
            )
        )
    }

    /**
     * Получить список напоминаний
     */
    fun listReminders(status: String? = null): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "list_reminders",
                arguments = buildJsonObject {
                    status?.let { put("status", it) }
                }
            )
        )
    }

    /**
     * Получить конкретное напоминание по ID
     */
    fun getReminder(id: Int): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_reminder",
                arguments = buildJsonObject {
                    put("id", id)
                }
            )
        )
    }

    /**
     * Пометить напоминание как выполненное
     */
    fun completeReminder(id: Int): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "complete_reminder",
                arguments = buildJsonObject {
                    put("id", id)
                }
            )
        )
    }

    /**
     * Удалить напоминание
     */
    fun deleteReminder(id: Int): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "delete_reminder",
                arguments = buildJsonObject {
                    put("id", id)
                }
            )
        )
    }

    /**
     * Получить сводку по всем напоминаниям
     */
    fun getRemindersSummary(): McpToolCallRequest {
        return McpToolCallRequest(
            id = System.currentTimeMillis().toString(),
            params = ToolCallParams(
                name = "get_reminders_summary",
                arguments = buildJsonObject {}
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