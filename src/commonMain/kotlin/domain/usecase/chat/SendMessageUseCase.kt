package domain.usecase.chat

import data.repository.ChatRepository

/**
 * Use Case для отправки сообщения пользователя в чат.
 * Инкапсулирует бизнес-логику отправки сообщения с учетом истории.
 *
 * @property chatRepository Репозиторий для работы с чатом
 */
class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Отправляет сообщение пользователя в чат с учетом истории
     *
     * @param message Текст сообщения пользователя
     * @return Result<Unit> - успех или ошибка
     */
    suspend operator fun invoke(message: String): Result<Unit> {
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
            chatRepository.sendUserMessageWithHistory(message.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 10000
    }
}