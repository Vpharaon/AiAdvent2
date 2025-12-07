package domain.usecase.eventplanner

import data.repository.EventPlannerRepository
import domain.Message

/**
 * Use Case для инициализации диалога с планировщиком мероприятий.
 * Отправляет системный промпт и получает приветственное сообщение от менеджера.
 *
 * @property eventPlannerRepository Репозиторий для работы с планировщиком
 */
class StartEventPlanConversationUseCase(
    private val eventPlannerRepository: EventPlannerRepository
) {
    /**
     * Инициализирует диалог с планировщиком мероприятий
     *
     * @return Result с первым сообщением ассистента
     */
    suspend operator fun invoke(): Result<Message> {
        return try {
            eventPlannerRepository.startConversation()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}