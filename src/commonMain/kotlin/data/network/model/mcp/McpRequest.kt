package data.network.model.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Базовый MCP JSON-RPC 2.0 запрос
 */
@Serializable
data class McpRequest(
    val jsonrpc: String = "2.0",
    val id: String,
    val method: String,
    val params: JsonObject? = null
)