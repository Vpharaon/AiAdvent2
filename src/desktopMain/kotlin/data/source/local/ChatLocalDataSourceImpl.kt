package data.source.local

import domain.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Desktop реализация источника данных для локального хранения истории чата
 */
class ChatLocalDataSourceImpl : ChatLocalDataSource {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val chatHistoryFile = File(CHAT_HISTORY_FILE_PATH)

    companion object {
        private const val CHAT_HISTORY_FILE_PATH = "chat_history.json"
    }

    /**
     * Сохраняет историю сообщений в файл
     */
    override suspend fun saveChatHistory(messages: List<Message>) = withContext(Dispatchers.IO) {
        try {
            // Конвертируем Message в сериализуемую модель
            val serializableMessages = messages.map { it.toSerializable() }
            val jsonString = json.encodeToString(serializableMessages)
            chatHistoryFile.writeText(jsonString)
        } catch (e: Exception) {
            // В случае ошибки логируем (можно добавить более продвинутую обработку)
            e.printStackTrace()
        }
    }

    /**
     * Загружает историю сообщений из файла
     */
    override suspend fun loadChatHistory(): List<Message> = withContext(Dispatchers.IO) {
        try {
            if (!chatHistoryFile.exists()) {
                return@withContext emptyList()
            }

            val jsonString = chatHistoryFile.readText()
            val serializableMessages = json.decodeFromString<List<SerializableMessage>>(jsonString)
            serializableMessages.map { it.toDomain() }
        } catch (e: Exception) {
            // В случае ошибки возвращаем пустой список
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Очищает содержимое файла с историей чата
     */
    override suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        try {
            // Записываем пустой массив вместо удаления файла
            val emptyJson = json.encodeToString(emptyList<SerializableMessage>())
            chatHistoryFile.writeText(emptyJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Сериализуемая модель сообщения для сохранения в JSON
     */
    @Serializable
    private data class SerializableMessage(
        val id: String,
        val content: String,
        val role: String,
        val timestamp: Long,
        val promptTokens: Int? = null,
        val completionTokens: Int? = null,
        val totalTokens: Int? = null
    )

    /**
     * Конвертирует Message в SerializableMessage
     */
    private fun Message.toSerializable() = SerializableMessage(
        id = id,
        content = content,
        role = role,
        timestamp = timestamp,
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        totalTokens = totalTokens
    )

    /**
     * Конвертирует SerializableMessage в Message
     */
    private fun SerializableMessage.toDomain() = Message(
        id = id,
        content = content,
        role = role,
        timestamp = timestamp,
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        totalTokens = totalTokens
    )
}