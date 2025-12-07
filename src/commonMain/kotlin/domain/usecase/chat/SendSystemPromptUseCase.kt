package domain.usecase.chat

import data.repository.ChatRepository

/**
 * Use Case для отправки системного промпта.
 * Инкапсулирует бизнес-логику инициализации чата с системным промптом.
 *
 * @property chatRepository Репозиторий для работы с чатом
 */
class SendSystemPromptUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Отправляет системный промпт и получает приветственное сообщение
     *
     * @param systemPrompt Текст системного промпта
     * @return Result<Unit> - успех или ошибка
     */
    suspend operator fun invoke(systemPrompt: String): Result<Unit> {
        return try {
            // Валидация
            if (systemPrompt.isBlank()) {
                return Result.failure(IllegalArgumentException("Системный промпт не может быть пустым"))
            }

            // Отправка системного промпта
            chatRepository.sendSystemPromptWithHistory(systemPrompt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}