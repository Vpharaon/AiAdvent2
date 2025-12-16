package data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Определение инструмента (tool) для LLM.
 * Используется для передачи доступных функций в запросе к LLM.
 */
@Serializable
data class Tool(
    // Тип инструмента, обычно "function"
    @SerialName("type")
    val type: String = "function",

    // Определение функции
    @SerialName("function")
    val function: FunctionDefinition
)

/**
 * Определение функции для LLM
 */
@Serializable
data class FunctionDefinition(
    // Название функции
    val name: String,

    // Описание функции
    val description: String,

    // JSON Schema параметров функции
    val parameters: JsonObject
)

/**
 * Вызов инструмента от LLM
 */
@Serializable
data class ToolCall(
    // Идентификатор вызова
    val id: String,

    // Тип вызова, обычно "function"
    val type: String = "function",

    // Данные о вызываемой функции
    val function: FunctionCall
)

/**
 * Данные о вызываемой функции
 */
@Serializable
data class FunctionCall(
    // Название функции
    val name: String,

    // Аргументы в формате JSON строки
    val arguments: String
)