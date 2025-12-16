package data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Представляет одно сообщение в чате.
 */
@Serializable
data class ChatMessage(
    // Роль автора сообщения: "system", "user", "assistant", или "tool".
    val role: MessageRole? = null,

    // Содержимое сообщения.
    val content: String? = null,

    // Вызовы инструментов (когда LLM хочет вызвать функцию)
    @SerialName("tool_calls")
    val toolCalls: List<ToolCall>? = null,

    // ID вызова инструмента (для ответа от инструмента)
    @SerialName("tool_call_id")
    val toolCallId: String? = null,

    // Название инструмента (для ответа от инструмента)
    val name: String? = null
)