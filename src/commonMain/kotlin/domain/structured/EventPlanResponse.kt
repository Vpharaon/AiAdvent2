package domain.structured

import kotlinx.serialization.Serializable

/**
 * Структурированный ответ от менеджера ресторана с планом корпоратива
 */
@Serializable
data class EventPlanResponse(
    val eventName: String,
    val guestCount: Int,
    val budget: String,
    val menuPreferences: List<String>,
    val drinkPreferences: List<String>,
    val eventDate: String,
    val eventDuration: String,
    val specialRequests: List<String>,
    val recommendations: List<String>,
    val totalEstimate: String
)

/**
 * План корпоратива с raw JSON для отладки
 */
data class EventPlanWithRaw(
    val eventPlan: EventPlanResponse,
    val rawJson: String,
    val fullResponseJson: String
)