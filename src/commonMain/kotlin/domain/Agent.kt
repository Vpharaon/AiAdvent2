package domain

import domain.agent.*

/**
 * Модель агента - специализированного AI ассистента с предопределенной ролью и промптом.
 *
 * Агенты используются для создания контекстно-зависимых диалогов, где AI принимает
 * определенную роль (например, менеджер ресторана, шеф-повар) и ведет беседу
 * в соответствии с этой ролью.
 *
 * @property id Уникальный идентификатор агента (используется для сохранения выбранного агента)
 * @property name Отображаемое имя агента (показывается в UI)
 * @property description Краткое описание функций агента (показывается в селекторе)
 * @property icon Emoji иконка агента для визуальной идентификации
 * @property systemPrompt Системный промпт, который определяет поведение и роль агента.
 *                        Этот промпт отправляется в начале каждого диалога и задает контекст
 *                        для всех последующих ответов AI.
 *
 * @sample
 * ```kotlin
 * val customAgent = Agent(
 *     id = "translator",
 *     name = "Переводчик",
 *     description = "Перевод текстов на разные языки",
 *     icon = "🌐",
 *     systemPrompt = "Ты - профессиональный переводчик. Переводи тексты точно и естественно."
 * )
 * ```
 */
data class Agent(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val systemPrompt: String
) {
    /**
     * Компаньон объект с утилитарными методами для работы с агентами.
     *
     * Все агенты теперь определены в отдельных файлах в пакете domain.agent:
     * - RestaurantManagerAgent - организация мероприятий
     * - ChefAgent - помощь в приготовлении блюд
     * - GeneralAssistantAgent - AI-наставник для обучения
     * - AndroidAiAgent - эксперт по Android разработке
     */
    companion object {
        /**
         * Возвращает список всех доступных предопределенных агентов.
         *
         * @return Список агентов в порядке приоритета отображения в UI
         */
        fun getAll(): List<Agent> = listOf(
            GeneralAssistantAgent,
            AndroidAiAgent,
            RestaurantManagerAgent,
            ChefAgent
        )

        /**
         * Находит агента по его уникальному идентификатору.
         *
         * @param id Уникальный идентификатор агента
         * @return Найденный агент или null, если агент с таким ID не существует
         */
        fun getById(id: String): Agent? = getAll().find { it.id == id }
    }
}