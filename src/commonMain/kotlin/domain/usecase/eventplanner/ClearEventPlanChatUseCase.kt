package domain.usecase.eventplanner

import data.repository.EventPlannerRepository

/**
 * Use Case для очистки истории диалога с планировщиком мероприятий.
 *
 * @property eventPlannerRepository Репозиторий для работы с планировщиком
 */
class ClearEventPlanChatUseCase(
    private val eventPlannerRepository: EventPlannerRepository
) {
    /**
     * Очищает историю диалога
     */
    operator fun invoke() {
        eventPlannerRepository.clearConversation()
    }
}