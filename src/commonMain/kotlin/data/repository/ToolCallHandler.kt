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

                // Task tools
                "add_task" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val title = args["title"]?.jsonPrimitive?.content
                    val description = args["description"]?.jsonPrimitive?.content
                        ?: return "Error: Description parameter is required"
                    val reminderTime = args["reminder_time"]?.jsonPrimitive?.content
                        ?: return "Error: reminder_time parameter is required"
                    val recurrence = args["recurrence"]?.jsonPrimitive?.content
                    val importance = args["importance"]?.jsonPrimitive?.content ?: "MEDIUM"

                    val result = reminderService.addReminder(title, description, reminderTime, recurrence, importance)
                    result.getOrElse { error ->
                        "Error adding task: ${error.message}"
                    }
                }

                "list_tasks" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val status = args["status"]?.jsonPrimitive?.content

                    val result = reminderService.listReminders(status)
                    result.getOrElse { error ->
                        "Error listing tasks: ${error.message}"
                    }
                }

                "get_task" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val id = args["id"]?.jsonPrimitive?.int
                        ?: return "Error: ID parameter is required"

                    val result = reminderService.getReminder(id)
                    result.getOrElse { error ->
                        "Error getting task: ${error.message}"
                    }
                }

                "complete_task" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val id = args["id"]?.jsonPrimitive?.int
                        ?: return "Error: ID parameter is required"

                    val result = reminderService.completeReminder(id)
                    result.getOrElse { error ->
                        "Error completing task: ${error.message}"
                    }
                }

                "delete_task" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val id = args["id"]?.jsonPrimitive?.int
                        ?: return "Error: ID parameter is required"

                    val result = reminderService.deleteReminder(id)
                    result.getOrElse { error ->
                        "Error deleting task: ${error.message}"
                    }
                }

                "get_tasks_for_date" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val date = args["date"]?.jsonPrimitive?.content
                        ?: return "Error: date parameter is required"

                    val result = reminderService.getTasksForDate(date)
                    result.getOrElse { error ->
                        "Error getting tasks for date: ${error.message}"
                    }
                }

                "get_tasks_by_importance" -> {
                    val args = json.parseToJsonElement(toolCall.function.arguments).jsonObject
                    val importance = args["importance"]?.jsonPrimitive?.content
                        ?: return "Error: importance parameter is required"

                    val result = reminderService.getTasksByImportance(importance)
                    result.getOrElse { error ->
                        "Error getting tasks by importance: ${error.message}"
                    }
                }

                "get_tasks_summary" -> {
                    val result = reminderService.getRemindersSummary()
                    result.getOrElse { error ->
                        "Error getting tasks summary: ${error.message}"
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