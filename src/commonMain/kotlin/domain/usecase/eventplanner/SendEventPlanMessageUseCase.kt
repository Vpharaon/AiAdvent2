package domain.usecase.eventplanner

import data.repository.EventPlannerRepository
import domain.structured.EventPlanWithRaw

/**
 * Use Case для отправки сообщения в диалог с планировщиком мероприятий.
 * Инкапсулирует бизнес-логику отправки сообщения с попыткой парсинга плана.
 *
 * @property eventPlannerRepository Репозиторий для работы с планировщиком
 */
class SendEventPlanMessageUseCase(
    private val eventPlannerRepository: EventPlannerRepository
) {
    /**
     * Отправляет сообщение пользователя в диалог
     *
     * @param message Текст сообщения пользователя
     * @return Result с опциональным распарсенным планом мероприятия
     */
    suspend operator fun invoke(message: String): Result<EventPlanWithRaw?> {
        return try {
            // Валидация входных данных
            if (message.isBlank()) {
                return Result.failure(IllegalArgumentException("Сообщение не может быть пустым"))
            }

            if (message.length > MAX_MESSAGE_LENGTH) {
                return Result.failure(
                    IllegalArgumentException("Сообщение слишком длинное (максимум $MAX_MESSAGE_LENGTH символов)")
                )
            }

            // Отправка сообщения через репозиторий
            eventPlannerRepository.sendMessage(message.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}