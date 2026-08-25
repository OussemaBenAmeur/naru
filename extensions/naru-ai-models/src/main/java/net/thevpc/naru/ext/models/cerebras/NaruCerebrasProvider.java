package net.thevpc.naru.ext.models.cerebras;

import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.ext.models.NaruModelCapabilitiesImpl;
import net.thevpc.naru.ext.models.openapi.AbstractOpenAICompatProvider;

/**
 * Cerebras provider — free-tier, high-speed inference for open models
 * through Cerebras' OpenAI-compatible API.
 *
 * <p>Endpoint: POST {baseUrl}/chat/completions
 * <p>Default baseUrl: https://api.cerebras.ai/v1
 */
public class NaruCerebrasProvider extends AbstractOpenAICompatProvider {

    public NaruCerebrasProvider() {
        super("cerebras");
        // Common production models available on Cerebras' free tier (2026)
        supportedModels.add("llama-3.3-70b");
        supportedModels.add("llama3.1-8b");
        supportedModels.add("qwen-3-32b");
        supportedModels.add("gpt-oss-120b");
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://api.cerebras.ai/v1";
    }

    @Override
    protected NaruModelCapabilities resolveCapabilities(String modelName) {
        boolean vision = false;
        boolean tools = !modelName.contains("qwen-3") || modelName.contains("gpt-oss");
        boolean thinking = modelName.contains("qwen-3") || modelName.contains("gpt-oss");
        boolean embedding = false;
        long contextLength = 131072L; // 128K standard for most Cerebras-hosted models

        if (modelName.contains("qwen-3")) {
            contextLength = 65536L; // 64K for Qwen 3 line
        }
        if (modelName.contains("gpt-oss")) {
            contextLength = 131072L; // 128K for gpt-oss
        }

        return new NaruModelCapabilitiesImpl(vision, tools, thinking, embedding, contextLength);
    }
}
