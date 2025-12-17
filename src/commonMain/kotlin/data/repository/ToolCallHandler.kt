package data.repository

import data.network.model.ChatMessage
import data.network.model.MessageRole
import data.network.model.ToolCall
import data.service.McpWeatherService
import data.service.McpReminderService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

/**
 * Обработчик вызовов инструментов (tool calls) от LLM
 */
class ToolCallHandler(
    private val weatherService: McpWeatherService,
    private val reminderService: McpReminderService
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

                // Reminder tools
                "add_reminder" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val title = args["title"]?.jsonPrimitive?.content
                        ?: return "Error: Title parameter is required"
                    val description = args["description"]?.jsonPrimitive?.content
                        ?: return "Error: Description parameter is required"
                    val dueDate = args["due_date"]?.jsonPrimitive?.content
                    val priority = args["priority"]?.jsonPrimitive?.content ?: "MEDIUM"

                    val result = reminderService.addReminder(title, description, dueDate, priority)
                    result.getOrElse { error ->
                        "Error adding reminder: ${error.message}"
                    }
                }

                "list_reminders" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val status = args["status"]?.jsonPrimitive?.content

                    val result = reminderService.listReminders(status)
                    result.getOrElse { error ->
                        "Error listing reminders: ${error.message}"
                    }
                }

                "get_reminder" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val id = args["id"]?.jsonPrimitive?.int
                        ?: return "Error: ID parameter is required"

                    val result = reminderService.getReminder(id)
                    result.getOrElse { error ->
                        "Error getting reminder: ${error.message}"
                    }
                }

                "complete_reminder" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val id = args["id"]?.jsonPrimitive?.int
                        ?: return "Error: ID parameter is required"

                    val result = reminderService.completeReminder(id)
                    result.getOrElse { error ->
                        "Error completing reminder: ${error.message}"
                    }
                }

                "delete_reminder" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val id = args["id"]?.jsonPrimitive?.int
                        ?: return "Error: ID parameter is required"

                    val result = reminderService.deleteReminder(id)
                    result.getOrElse { error ->
                        "Error deleting reminder: ${error.message}"
                    }
                }

                "get_reminders_summary" -> {
                    val result = reminderService.getRemindersSummary()
                    result.getOrElse { error ->
                        "Error getting reminders summary: ${error.message}"
                    }
                }

                "set_notification_schedule" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val intervalMinutes = args["interval_minutes"]?.jsonPrimitive?.int
                        ?: return "Error: interval_minutes parameter is required"
                    val enabled = args["enabled"]?.jsonPrimitive?.content?.toBoolean() ?: true

                    val result = reminderService.setNotificationSchedule(intervalMinutes, enabled)
                    result.getOrElse { error ->
                        "Error setting notification schedule: ${error.message}"
                    }
                }

                "get_notification_schedule" -> {
                    val result = reminderService.getNotificationSchedule()
                    result.getOrElse { error ->
                        "Error getting notification schedule: ${error.message}"
                    }
                }

                "send_test_notification" -> {
                    val result = reminderService.sendTestNotification()
                    result.getOrElse { error ->
                        "Error sending test notification: ${error.message}"
                    }
                }

                else -> "Error: Unknown tool '${toolCall.function.name}'"
            }
        } catch (e: Exception) {
            "Error executing tool '${toolCall.function.name}': ${e.message}"
        }
    }
}