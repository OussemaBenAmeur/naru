package net.thevpc.naru.ext.models.groq;

import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.ext.models.NaruModelCapabilitiesImpl;
import net.thevpc.naru.ext.models.openapi.AbstractOpenAICompatProvider;

/**
 * Groq provider — free-tier, ultra-fast inference for open models
 * through Groq's OpenAI-compatible API.
 *
 * <p>Endpoint: POST {baseUrl}/chat/completions
 * <p>Default baseUrl: https://api.groq.com/openai/v1
 */
public class NaruGroqProvider extends AbstractOpenAICompatProvider {

    public NaruGroqProvider() {
        super("groq");
        // Common production models available on Groq's free tier (2026)
        supportedModels.add("llama-3.3-70b-versatile");
        supportedModels.add("llama-3.1-8b-instant");
        supportedModels.add("openai/gpt-oss-120b");
        supportedModels.add("openai/gpt-oss-20b");
        supportedModels.add("qwen/qwen3-32b");
        supportedModels.add("moonshotai/kimi-k2-instruct");
        supportedModels.add("deepseek-r1-distill-llama-70b");
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://api.groq.com/openai/v1";
    }

    @Override
    protected NaruModelCapabilities resolveCapabilities(String modelName) {
        boolean vision = modelName.contains("maverick") || modelName.contains("scout");
        boolean tools = !modelName.contains("deepseek-r1-distill");
        boolean thinking = modelName.contains("qwen3") || modelName.contains("deepseek-r1") || modelName.contains("kimi-k2");
        boolean embedding = false;
        long contextLength = 131072L; // 128K standard for most Groq-hosted models

        if (modelName.contains("kimi-k2")) {
            contextLength = 262144L; // 256K for Kimi K2
        }

        return new NaruModelCapabilitiesImpl(vision, tools, thinking, embedding, contextLength);
    }
}
