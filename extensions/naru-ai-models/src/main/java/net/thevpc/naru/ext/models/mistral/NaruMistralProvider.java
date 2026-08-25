package net.thevpc.naru.ext.models.mistral;

import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.api.model.NaruModelConfig;
import net.thevpc.naru.api.model.NaruModelProtocol;
import net.thevpc.naru.ext.models.NaruModelCapabilitiesImpl;
import net.thevpc.naru.ext.models.openapi.AbstractOpenAICompatProvider;

public class NaruMistralProvider extends AbstractOpenAICompatProvider {

    public NaruMistralProvider() {
        super("mistral");
        // Populating common production-tier Mistral models available in 2026
        supportedModels.add("mistral-medium-3.5");
        supportedModels.add("mistral-small-4");
        supportedModels.add("mistral-large-latest");
        supportedModels.add("codestral-latest");
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://api.mistral.ai/v1";
    }

    @Override
    protected NaruModelProtocol createProtocol(NaruModelConfig model, NaruModelCapabilities capabilities) {
        return new NaruModelProtocolMistral(this, model, name(), capabilities);
    }

    /**
     * Statically maps model limits since cloud-hosted capabilities cannot be polled natively.
     */
    @Override
    protected NaruModelCapabilities resolveCapabilities(String modelName) {
        boolean vision = modelName.contains("small-4") || modelName.contains("medium-3.5");
        boolean tools = true;
        boolean thinking = modelName.contains("medium");
        boolean embedding = false;

        // 2026 standard context limits for modern Mistral architectures
        long contextLength = 262144L; // Default 262K context window (e.g. Large 3 / Medium 3.5)

        if (modelName.contains("small-4")) {
            contextLength = 256000L; // 256K for the unified small lines
        }

        return new NaruModelCapabilitiesImpl(vision, tools, thinking, embedding, contextLength);
    }
}
