package data.network.model.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Базовый MCP JSON-RPC 2.0 ответ
 */
@Serializable
data class McpResponse(
    val jsonrpc: String,
    val id: String,
    val result: JsonElement? = null,
    val error: McpError? = null
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)