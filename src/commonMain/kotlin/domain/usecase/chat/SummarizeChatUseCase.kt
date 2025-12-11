package domain.usecase.chat

import data.network.model.ChatMessage
import data.network.model.MessageRole
import data.repository.ChatRepository
import domain.Message

/**
 * Use Case для создания сжатой версии истории диалога.
 *
 * Функциональность:
 * - Получает всю историю сообщений
 * - Отправляет промпт на создание сводки/резюме диалога
 * - Заменяет все сообщения одним сжатым резюме
 *
 * @property chatRepository Репозиторий для работы с чатом
 */
class SummarizeChatUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Создает сжатую версию истории диалога
     *
     * @return Result<Unit> - успех или ошибка
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Получаем текущие сообщения
            val currentMessages = chatRepository.messages.value

            // Проверяем, есть ли сообщения для сжатия
            if (currentMessages.isEmpty()) {
                return Result.failure(IllegalStateException("Нет сообщений для сжатия"))
            }

            // Отправляем запрос на сжатие истории
            chatRepository.summarizeHistory()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}