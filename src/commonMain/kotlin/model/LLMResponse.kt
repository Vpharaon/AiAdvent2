package model

import kotlinx.serialization.Serializable

@Serializable
data class LLMResponse(
    val message: String,
    val success: Boolean
)