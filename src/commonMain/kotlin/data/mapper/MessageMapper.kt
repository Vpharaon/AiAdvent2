package data.mapper

import data.network.model.ChatMessage
import data.network.model.MessageRole
import domain.Message

/**
 * Mapper для конвертации между Network DTOs и Domain моделями сообщений.
 * Разделяет слои Data и Domain.
 */
object MessageMapper {

    /**
     * Конвертирует Domain Message в Network ChatMessage
     *
     * @param domainMessage Доменная модель сообщения
     * @return Network DTO сообщения
     */
    fun toNetworkModel(domainMessage: Message): ChatMessage {
        return ChatMessage(
            role = MessageRole.valueOf(domainMessage.role.uppercase()),
            content = domainMessage.content
        )
    }

    /**
     * Конвертирует список Domain Messages в список Network ChatMessages
     *
     * @param domainMessages Список доменных сообщений
     * @return Список Network DTOs
     */
    fun toNetworkModels(domainMessages: List<Message>): List<ChatMessage> {
        return domainMessages.map { toNetworkModel(it) }
    }

    /**
     * Конвертирует Network ChatMessage в Domain Message
     *
     * @param chatMessage Network DTO сообщения
     * @param id Идентификатор сообщения
     * @param timestamp Временная метка
     * @return Доменная модель сообщения
     */
    fun toDomainModel(
        chatMessage: ChatMessage,
        id: String,
        timestamp: Long
    ): Message {
        return Message(
            id = id,
            content = chatMessage.content.orEmpty(),
            role = chatMessage.role?.value ?: MessageRole.ASSISTANT.value,
            timestamp = timestamp
        )
    }

    /**
     * Создает Domain Message из параметров
     *
     * @param id Идентификатор сообщения
     * @param content Содержание сообщения
     * @param role Роль отправителя
     * @param timestamp Временная метка
     * @return Доменная модель сообщения
     */
    fun createDomainMessage(
        id: String,
        content: String,
        role: MessageRole,
        timestamp: Long
    ): Message {
        return Message(
            id = id,
            content = content,
            role = role.value,
            timestamp = timestamp
        )
    }
}