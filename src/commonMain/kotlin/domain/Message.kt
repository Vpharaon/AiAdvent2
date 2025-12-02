package domain

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Доменная модель сообщения в чате.
 *
 * @property id Уникальный идентификатор сообщения
 * @property content Текст сообщения
 * @property timestamp Временная метка отправки сообщения (в миллисекундах с эпохи Unix)
 * @property role Роль отправителя сообщения (system, user, assistant)
 * @property isUser Флаг, указывающий является ли сообщение от пользователя
 * @property formattedTime Отформатированное время в виде строки (HH:mm)
 */
@Serializable
data class Message(
    val id: String,
    val content: String,
    val timestamp: Long,
    val role: String
) {
    val isUser: Boolean
        get() = role == "user"

    val formattedTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}