package net.thevpc.naru.ext.models.github;

import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.ext.models.NaruModelCapabilitiesImpl;
import net.thevpc.naru.ext.models.openapi.AbstractOpenAICompatProvider;

/**
 * GitHub Models provider — free inference for a curated catalog of models
 * using any GitHub Personal Access Token (PAT).
 *
 * <p>Endpoint: POST {baseUrl}/chat/completions
 * <p>Default baseUrl: https://models.github.ai/inference
 * <p>Model ids are publisher-qualified, e.g. {@code openai/gpt-4o-mini}.
 */
public class NaruGitHubModelsProvider extends AbstractOpenAICompatProvider {

    public NaruGitHubModelsProvider() {
        super("github");
        // Common catalog models available on GitHub Models free tier (2026)
        supportedModels.add("openai/gpt-4o-mini");
        supportedModels.add("openai/gpt-4.1-mini");
        supportedModels.add("meta/Llama-3.3-70B-Instruct");
        supportedModels.add("meta/Llama-4-Scout-17B-16E-Instruct");
        supportedModels.add("mistral-ai/mistral-small-2503");
        supportedModels.add("deepseek/DeepSeek-R1");
        supportedModels.add("microsoft/Phi-4");
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://models.github.ai/inference";
    }

    @Override
    protected NaruModelCapabilities resolveCapabilities(String modelName) {
        boolean vision = modelName.contains("gpt-4o") || modelName.contains("gpt-4.1") || modelName.contains("Scout");
        boolean tools = !modelName.contains("DeepSeek-R1");
        boolean thinking = modelName.contains("DeepSeek-R1") || modelName.contains("o1") || modelName.contains("o3");
        boolean embedding = false;
        long contextLength = 131072L; // 128K standard across the GitHub Models catalog

        if (modelName.contains("Phi-4")) {
            contextLength = 16384L; // 16K for Phi-4
        }

        return new NaruModelCapabilitiesImpl(vision, tools, thinking, embedding, contextLength);
    }
}
