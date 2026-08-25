package net.thevpc.naru.ext.models.gemini;

import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.ext.models.NaruModelCapabilitiesImpl;
import net.thevpc.naru.ext.models.openapi.AbstractOpenAICompatProvider;

/**
 * Gemini provider — talks to Google AI Studio through the OpenAI-compatible router.
 *
 * <p>Endpoint: POST {baseUrl}/chat/completions
 * <p>Default baseUrl: https://generativelence.googleapis.com/v1beta/openai
 */
public class NaruGeminiProvider extends AbstractOpenAICompatProvider {

    public NaruGeminiProvider() {
        super("gemini");
        // Populating common production-tier Gemini models available in 2026
        supportedModels.add("gemini-2.5-flash");
        supportedModels.add("gemini-3.5-flash");
        supportedModels.add("gemini-1.5-pro");
        supportedModels.add("gemini-2.5-pro");
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/openai";
    }

    /**
     * Statically maps model limits since cloud-hosted capabilities cannot be polled natively.
     */
    @Override
    protected NaruModelCapabilities resolveCapabilities(String modelName) {
        boolean vision = true;
        boolean tools = true;
        boolean thinking = modelName.contains("pro");
        boolean embedding = false;
        long contextLength = 1048576L; // 1M tokens standard fallback for Flash lines

        if (modelName.contains("1.5-pro") || modelName.contains("2.5-pro")) {
            contextLength = 2097152L; // 2M tokens context window for Pro tiers
        }

        return new NaruModelCapabilitiesImpl(vision, tools, thinking, embedding, contextLength);
    }
}
