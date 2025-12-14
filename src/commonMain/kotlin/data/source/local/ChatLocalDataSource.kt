package data.source.local

import domain.Message

/**
 * Интерфейс для работы с локальным хранилищем истории чата
 */
interface ChatLocalDataSource {
    /**
     * Сохраняет историю сообщений в файл
     * @param messages Список сообщений для сохранения
     */
    suspend fun saveChatHistory(messages: List<Message>)

    /**
     * Загружает историю сообщений из файла
     * @return Список сообщений или пустой список, если файл не существует
     */
    suspend fun loadChatHistory(): List<Message>

    /**
     * Очищает содержимое файла с историей чата (записывает пустой массив)
     */
    suspend fun clearChatHistory()
}