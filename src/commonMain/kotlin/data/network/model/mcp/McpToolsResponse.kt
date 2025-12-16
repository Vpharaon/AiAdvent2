package data.network.model.mcp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Ответ со списком доступных MCP инструментов
 */
@Serializable
data class McpToolsResponse(
    val jsonrpc: String,
    val id: String,
    val result: ToolsResult,
    val error: McpError? = null
)

@Serializable
data class ToolsResult(
    val tools: List<McpTool>
)

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)