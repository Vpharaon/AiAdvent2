package data.service

import data.network.McpApiClient
import data.network.model.mcp.ReminderToolRequestBuilder
import domain.ApiError

/**
 * Сервис для работы с напоминаниями через MCP сервер
 */
class McpReminderService(
    private val mcpClient: McpApiClient = McpApiClient()
) {
    /**
     * Создать новое напоминание
     *
     * @param title Краткое название задачи
     * @param description Детальное описание
     * @param dueDate Срок выполнения в ISO формате (2024-12-17T15:30:00)
     * @param priority Приоритет - LOW, MEDIUM, HIGH, URGENT (по умолчанию MEDIUM)
     * @return Результат создания напоминания или ошибка
     */
    suspend fun addReminder(
        title: String,
        description: String,
        dueDate: String? = null,
        priority: String = "MEDIUM"
    ): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.addReminder(title, description, dueDate, priority)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in add reminder response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to add reminder", cause = e))
        }
    }

    /**
     * Получить список всех напоминаний или отфильтровать по статусу
     *
     * @param status Фильтр по статусу - ACTIVE, COMPLETED, ARCHIVED
     * @return Список напоминаний или ошибка
     */
    suspend fun listReminders(status: String? = null): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.listReminders(status)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in list reminders response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to list reminders", cause = e))
        }
    }

    /**
     * Получить детальную информацию о конкретном напоминании
     *
     * @param id ID напоминания
     * @return Информация о напоминании или ошибка
     */
    suspend fun getReminder(id: Int): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.getReminder(id)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in get reminder response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get reminder", cause = e))
        }
    }

    /**
     * Пометить напоминание как выполненное
     *
     * @param id ID напоминания
     * @return Результат операции или ошибка
     */
    suspend fun completeReminder(id: Int): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.completeReminder(id)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in complete reminder response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to complete reminder", cause = e))
        }
    }

    /**
     * Удалить напоминание
     *
     * @param id ID напоминания
     * @return Результат операции или ошибка
     */
    suspend fun deleteReminder(id: Int): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.deleteReminder(id)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in delete reminder response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to delete reminder", cause = e))
        }
    }

    /**
     * Получить сводку по всем напоминаниям (статистика, просроченные, приоритетные, предстоящие)
     *
     * @return Сводка по напоминаниям или ошибка
     */
    suspend fun getRemindersSummary(): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.getRemindersSummary()
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in summary response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get reminders summary", cause = e))
        }
    }

    /**
     * Настроить автоматические периодические уведомления с сводкой по напоминаниям
     *
     * @param intervalMinutes Интервал в минутах (60 = каждый час, 1440 = раз в день)
     * @param enabled Включить/выключить уведомления (по умолчанию true)
     * @return Результат настройки или ошибка
     */
    suspend fun setNotificationSchedule(intervalMinutes: Int, enabled: Boolean = true): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.setNotificationSchedule(intervalMinutes, enabled)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in set schedule response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to set notification schedule", cause = e))
        }
    }

    /**
     * Получить текущие настройки расписания уведомлений
     *
     * @return Настройки расписания или ошибка
     */
    suspend fun getNotificationSchedule(): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.getNotificationSchedule()
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in get schedule response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get notification schedule", cause = e))
        }
    }

    /**
     * Отправить тестовое уведомление немедленно для проверки конфигурации
     *
     * @return Результат отправки или ошибка
     */
    suspend fun sendTestNotification(): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.sendTestNotification()
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in test notification response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to send test notification", cause = e))
        }
    }

    /**
     * Закрыть клиент
     */
    fun close() {
        mcpClient.close()
    }
}