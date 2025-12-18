package data.service

import data.network.McpApiClient
import data.network.model.mcp.ReminderToolRequestBuilder
import domain.ApiError

/**
 * Сервис для работы с задачами через MCP сервер
 */
class McpReminderService(
    private val mcpClient: McpApiClient = McpApiClient()
) {
    /**
     * Создать новую задачу
     *
     * @param title Краткое название задачи (опционально, может быть автоматически сгенерирован из описания)
     * @param description Детальное описание задачи
     * @param reminderTime Время напоминания в ISO формате (2024-12-17T15:30:00)
     * @param recurrence Повторение - DAILY, WEEKLY, MONTHLY (опционально)
     * @param importance Важность - LOW, MEDIUM, HIGH, URGENT (по умолчанию MEDIUM)
     * @return Результат создания задачи или ошибка
     */
    suspend fun addReminder(
        title: String? = null,
        description: String,
        reminderTime: String,
        recurrence: String? = null,
        importance: String = "MEDIUM"
    ): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.addReminder(title, description, reminderTime, recurrence, importance)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in add task response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to add task", cause = e))
        }
    }

    /**
     * Получить список всех задач или отфильтровать по статусу
     *
     * @param status Фильтр по статусу - ACTIVE, COMPLETED
     * @return Список задач или ошибка
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
                        Result.failure(ApiError.UnknownError(message = "No content in list tasks response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to list tasks", cause = e))
        }
    }

    /**
     * Получить детальную информацию о конкретной задаче
     *
     * @param id ID задачи
     * @return Информация о задаче или ошибка
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
                        Result.failure(ApiError.UnknownError(message = "No content in get task response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get task", cause = e))
        }
    }

    /**
     * Пометить задачу как выполненную
     *
     * @param id ID задачи
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
                        Result.failure(ApiError.UnknownError(message = "No content in complete task response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to complete task", cause = e))
        }
    }

    /**
     * Удалить задачу
     *
     * @param id ID задачи
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
                        Result.failure(ApiError.UnknownError(message = "No content in delete task response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to delete task", cause = e))
        }
    }

    /**
     * Получить все задачи на конкретную дату
     *
     * @param date Дата в ISO формате (2024-12-17)
     * @return Список задач на указанную дату или ошибка
     */
    suspend fun getTasksForDate(date: String): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.getTasksForDate(date)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in get tasks for date response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get tasks for date", cause = e))
        }
    }

    /**
     * Получить задачи отфильтрованные по уровню важности
     *
     * @param importance Уровень важности - LOW, MEDIUM, HIGH, URGENT
     * @return Список задач с указанным уровнем важности или ошибка
     */
    suspend fun getTasksByImportance(importance: String): Result<String> {
        return try {
            val request = ReminderToolRequestBuilder.getTasksByImportance(importance)
            val response = mcpClient.callTool(request)

            response.fold(
                onSuccess = { toolResponse ->
                    val content = toolResponse.result?.content?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(ApiError.UnknownError(message = "No content in get tasks by importance response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get tasks by importance", cause = e))
        }
    }

    /**
     * Получить сводку по всем задачам (статистика, просроченные, приоритетные, предстоящие)
     *
     * @return Сводка по задачам или ошибка
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
                        Result.failure(ApiError.UnknownError(message = "No content in tasks summary response"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(ApiError.UnknownError(message = e.message ?: "Failed to get tasks summary", cause = e))
        }
    }

    /**
     * Настроить автоматические периодические уведомления с сводкой по задачам
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