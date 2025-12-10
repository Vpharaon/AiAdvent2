package domain

import kotlinx.serialization.Serializable

/**
 * Провайдеры LLM моделей
 */
enum class LlmProvider(
    val displayName: String,
    val apiKeyName: String,
    val baseUrl: String
) {
    GLM("GLM", "glm", "https://api.z.ai/api/paas/v4/chat/completions"),
    OPENROUTER("OpenRouter", "openrouter", "https://openrouter.ai/api/v1/chat/completions"),
    OPENAI("OpenAI", "openai", "https://api.openai.com/v1/chat/completions"),
    DEEPSEEK("DeepSeek", "deepseek", "https://api.deepseek.com/v1/chat/completions");

    /**
     * Определяет провайдера по URL
     */
    companion object {
        fun fromUrl(url: String): LlmProvider? {
            return values().find { url.contains(it.baseUrl.substringAfter("://").substringBefore("/")) }
        }
    }
}

/**
 * Модель данных для LLM провайдера.
 *
 * @property id Уникальный идентификатор модели
 * @property name Отображаемое название модели
 * @property provider Провайдер модели
 * @property apiUrl URL эндпоинта API
 * @property modelName Техническое имя модели для использования в API запросах
 * @property apiKey API ключ для доступа к модели (опционально)
 * @property description Описание модели (опционально)
 */
@Serializable
data class LlmModel(
    val id: String,
    val name: String,
    val apiUrl: String,
    val modelName: String,
    val apiKey: String? = null,
    val description: String? = null
) {
    /**
     * Получает провайдера для данной модели
     */
    fun getProvider(): LlmProvider? = LlmProvider.fromUrl(apiUrl)
}

/**
 * Предопределенные LLM модели.
 */
object LlmModels {
    val GLM_4_6 = LlmModel(
        id = "glm-4-6",
        name = "GLM-4.6",
        apiUrl = "https://api.z.ai/api/paas/v4/chat/completions",
        modelName = "glm-4.6",
        description = "GLM-4.6 - продвинутая модель с высокой производительностью"
    )

    val GLM_4_5_AIR = LlmModel(
        id = "glm-4-5-air",
        name = "GLM-4.5 Air",
        apiUrl = "https://api.z.ai/api/paas/v4/chat/completions",
        modelName = "glm-4.5-air",
        description = "GLM-4.5 Air - облегченная версия с быстрым ответом"
    )

    val OPENAI_GPT_35_TURBO = LlmModel(
        id = "openai-gpt-3.5-turbo",
        name = "GPT-3.5 Turbo",
        apiUrl = "https://api.openai.com/v1/chat/completions",
        modelName = "gpt-3.5-turbo",
        description = "OpenAI GPT-3.5 Turbo - быстрая и экономичная модель"
    )

    val DEEPSEEK_CHAT = LlmModel(
        id = "deepseek-chat",
        name = "DeepSeek Chat",
        apiUrl = "https://api.deepseek.com/v1/chat/completions",
        modelName = "deepseek-chat",
        description = "DeepSeek Chat - универсальная модель для диалогов"
    )

    val DEEPSEEK_CODER = LlmModel(
        id = "deepseek-coder",
        name = "DeepSeek Coder",
        apiUrl = "https://api.deepseek.com/v1/chat/completions",
        modelName = "deepseek-coder",
        description = "DeepSeek Coder - специализированная модель для программирования"
    )

    // OpenRouter Models
    val OPENROUTER_GPT4_TURBO = LlmModel(
        id = "openrouter-gpt-4-turbo",
        name = "GPT-4 Turbo (OpenRouter)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "openai/gpt-4-turbo",
        description = "OpenAI GPT-4 Turbo через OpenRouter"
    )

    val OPENROUTER_CLAUDE_3_OPUS = LlmModel(
        id = "openrouter-claude-3-opus",
        name = "Claude 3 Opus (OpenRouter)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "anthropic/claude-3-opus",
        description = "Anthropic Claude 3 Opus через OpenRouter"
    )

    val OPENROUTER_CLAUDE_3_SONNET = LlmModel(
        id = "openrouter-claude-3-sonnet",
        name = "Claude 3 Sonnet (OpenRouter)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "anthropic/claude-3-sonnet",
        description = "Anthropic Claude 3 Sonnet через OpenRouter"
    )

    val OPENROUTER_GEMINI_PRO = LlmModel(
        id = "openrouter-gemini-pro",
        name = "Gemini Pro (OpenRouter)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "google/gemini-pro",
        description = "Google Gemini Pro через OpenRouter"
    )

    val OPENROUTER_MISTRAL_LARGE = LlmModel(
        id = "openrouter-mistral-large",
        name = "Mistral Large (OpenRouter)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "mistralai/mistral-large",
        description = "Mistral Large через OpenRouter"
    )

    val OPENROUTER_LLAMA_3_70B = LlmModel(
        id = "openrouter-llama-3-70b",
        name = "Llama 3 70B (OpenRouter)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "meta-llama/llama-3-70b-instruct",
        description = "Meta Llama 3 70B через OpenRouter"
    )

    // Бесплатные модели через OpenRouter
    val FREE_MISTRALAI_DEVSTRAL_2512 = LlmModel(
        id = "mistralai/devstral-2512:free",
        name = "Mistral: Devstral 2 2512",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "mistralai/devstral-2512:free",
        description = "🆓 Mistral Devstral 2 - бесплатная модель для кода и разработки"
    )

    val FREE_AMAZON_NOVA_2_LITE = LlmModel(
        id = "amazon/nova-2-lite-v1:free",
        name = "Amazon Nova 2 Lite",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "amazon/nova-2-lite-v1:free",
        description = "🆓 Amazon Nova 2 Lite - бесплатная легкая модель от Amazon"
    )

    val FREE_NVIDIA_NEMOTRON_NANO_12B = LlmModel(
        id = "nvidia/nemotron-nano-12b-v2-vl:free",
        name = "NVIDIA Nemotron Nano 12B",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "nvidia/nemotron-nano-12b-v2-vl:free",
        description = "🆓 NVIDIA Nemotron Nano 12B - бесплатная мультимодальная модель"
    )

    val FREE_OPENAI_GPT_OSS_120B = LlmModel(
        id = "openai/gpt-oss-120b:free",
        name = "OpenAI GPT OSS 120B",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "openai/gpt-oss-120b:free",
        description = "🆓 OpenAI GPT OSS 120B - бесплатная открытая модель от OpenAI"
    )

    val FREE_GLM_4_5_AIR = LlmModel(
        id = "z-ai/glm-4.5-air:free",
        name = "GLM 4.5 Air (Free)",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "z-ai/glm-4.5-air:free",
        description = "🆓 GLM 4.5 Air - бесплатная облегченная модель от Z.AI"
    )

    val FREE_QWEN3_CODER = LlmModel(
        id = "qwen/qwen3-coder:free",
        name = "Qwen 3 Coder",
        apiUrl = "https://openrouter.ai/api/v1/chat/completions",
        modelName = "qwen/qwen3-coder:free",
        description = "🆓 Qwen 3 Coder - бесплатная модель для программирования от Alibaba"
    )

    /**
     * Список бесплатных моделей.
     */
    val FREE_MODELS = listOf(
        FREE_MISTRALAI_DEVSTRAL_2512,
        FREE_AMAZON_NOVA_2_LITE,
        FREE_NVIDIA_NEMOTRON_NANO_12B,
        //FREE_OPENAI_GPT_OSS_120B,
        FREE_GLM_4_5_AIR,
        //FREE_QWEN3_CODER
    )

    /**
     * Список всех доступных моделей (платные).
     */
    val PAID_MODELS = listOf(
        GLM_4_6,
        GLM_4_5_AIR,
        OPENAI_GPT_35_TURBO,
        DEEPSEEK_CHAT,
        DEEPSEEK_CODER,
        OPENROUTER_GPT4_TURBO,
        OPENROUTER_CLAUDE_3_OPUS,
        OPENROUTER_CLAUDE_3_SONNET,
        OPENROUTER_GEMINI_PRO,
        OPENROUTER_MISTRAL_LARGE,
        OPENROUTER_LLAMA_3_70B
    )

    val CUSTOM_MODELS = listOf(DEEPSEEK_CHAT, GLM_4_6)

    /**
     * Список всех доступных моделей по умолчанию (только бесплатные).
     */
    val DEFAULT_MODELS = CUSTOM_MODELS
}