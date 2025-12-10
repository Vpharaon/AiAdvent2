package domain.service

import domain.LlmModel

/**
 * Интерфейс для подсчета токенов в тексте.
 * Использует expect/actual pattern для платформо-специфичных реализаций.
 */
interface ITokenCounter {
    /**
     * Подсчитывает количество токенов в тексте для указанной модели.
     *
     * @param text Текст для подсчета токенов
     * @param model LLM модель, для которой производится подсчет
     * @return Количество токенов или null, если подсчет недоступен
     */
    fun countTokens(text: String, model: LlmModel): Int?
}

/**
 * Expect функция для получения реализации TokenCounter для текущей платформы.
 * На платформах, где подсчет токенов недоступен, возвращает null.
 */
expect fun getTokenCounter(): ITokenCounter?