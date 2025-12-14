package domain.usecase.chat

import data.repository.ChatRepository

/**
 * Use Case для очистки истории чата.
 * Инкапсулирует бизнес-логику очистки чата.
 *
 * @property chatRepository Репозиторий для работы с чатом
 */
class ClearChatUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Очищает всю историю чата
     *
     * @return Result<Unit> - успех или ошибка
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            chatRepository.clearMessages()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}