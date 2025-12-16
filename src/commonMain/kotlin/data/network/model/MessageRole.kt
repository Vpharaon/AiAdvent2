package data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Enum для ролей в чате. Использование Enum безопаснее, чем строки, так как помогает избежать опечаток.
 */
@Serializable
enum class MessageRole(val value: String) {
    @SerialName("system")
    SYSTEM("system"),
    @SerialName("user")
    USER("user"),
    @SerialName("assistant")
    ASSISTANT("assistant"),
    @SerialName("tool")
    TOOL("tool");

    companion object {
        /**
         * Создает MessageRole из строкового значения.
         * @param value Строковое значение роли (например, "system", "user", "assistant", "tool")
         * @return MessageRole или null если значение не распознано
         */
        fun valueOf(value: String): MessageRole? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}