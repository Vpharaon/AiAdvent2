package data.network.model.mcp

import kotlinx.serialization.Serializable

/**
 * Ответ от вызова MCP инструмента
 */
@Serializable
data class McpToolCallResponse(
    val jsonrpc: String,
    val id: String,
    val result: ToolCallResult? = null,
    val error: McpError? = null
)

@Serializable
data class ToolCallResult(
    val content: List<ToolContent>
)

@Serializable
data class ToolContent(
    val text: String
)