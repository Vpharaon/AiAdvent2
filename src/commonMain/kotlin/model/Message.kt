package model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)