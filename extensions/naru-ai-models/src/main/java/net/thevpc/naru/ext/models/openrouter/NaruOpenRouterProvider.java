package net.thevpc.naru.ext.models.openrouter;

import net.thevpc.naru.api.agent.NaruSession;
import net.thevpc.naru.api.model.AbstractNaruModelProvider;
import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.api.model.NaruModelConfig;
import net.thevpc.naru.api.model.NaruModelProtocol;
import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.naru.ext.models.NaruModelCapabilitiesImpl;
import net.thevpc.naru.ext.models.openapi.NaruModelProtocolOpenAICompat;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.net.NWebCli;
import net.thevpc.nuts.net.NWebRequest;
import net.thevpc.nuts.net.NWebResponse;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.*;

/**
 * OpenRouter provider — one API key gives access to hundreds of models,
 * including a rotating set of {@code :free} variants.
 *
 * <p>Endpoint: POST {baseUrl}/chat/completions
 * <p>Default baseUrl: https://openrouter.ai/api/v1
 */
public class NaruOpenRouterProvider extends AbstractNaruModelProvider {

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";

    private final Map<NaruModelConfig, NaruModelProtocol> protocols = new HashMap<>();
    private final NElementReader nElementReader = NElementReader.ofJson();

    public NaruOpenRouterProvider() {
        super("openrouter");
    }

    @Override
    public NOptional<NaruModelProtocol> getProtocol(NaruModelConfig model, NaruSession session) {
        if (!model.provider().equals(name())) {
            return NOptional.ofNamedEmpty(NMsg.ofC("protocol for %s", model));
        }
        NaruModelCapabilities capabilities = resolveCapabilities(model.model());
        return NOptional.of(protocols.computeIfAbsent(model,
                k -> new NaruOpenRouterProtocol(this, model, name(), capabilities)
        ));
    }

    /**
     * Model list is dynamic: fetched live from GET /models.
     * Free ({@code :free}) variants are listed first.
     */
    @Override
    public List<String> findModelIds(NaruSession session) {
        NWebCli http = NWebCli.of()
                .connectTimeout(NDuration.ofSeconds(10))
                .baseUri(baseUrl(session));
        NWebRequest request = http.GET("models")
                .readTimeout(NDuration.ofSeconds(30));
        String apiKey = apiKey(session);
        if (!NBlankable.isBlank(apiKey)) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        try {
            NWebResponse response = request.run().ifErrorThrow();
            NElement root = nElementReader.read(response.contentAsString());
            List<String> free = new ArrayList<>();
            List<String> paid = new ArrayList<>();
            root.asObject().flatMap(o -> o.getArray("data")).ifPresent(arr -> {
                for (NElement el : arr.children()) {
                    el.asObject().flatMap(o -> o.getStringValue("id")).ifPresent(id -> {
                        (id.endsWith(":free") ? free : paid).add(id);
                    });
                }
            });
            List<String> all = new ArrayList<>(free);
            all.addAll(paid);
            return all;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    protected String baseUrl(NaruSession session) {
        return session.agent().env().get(name() + ".url")
                .flatMap(x -> x.asStringValue())
                .map(x -> x.replaceAll("/$", ""))
                .orElse(DEFAULT_BASE_URL);
    }

    protected String apiKey(NaruSession session) {
        return session.agent().env().get(name() + ".apiKey")
                .flatMap(x -> x.asStringValue()).orNull();
    }

    /**
     * Capabilities are unknown per-model without a catalog lookup;
     * assume text+tools (the most common case) and let tool-call
     * emulation handle models that lack native support.
     */
    private NaruModelCapabilities resolveCapabilities(String modelName) {
        boolean vision = modelName.contains("vision") || modelName.contains("vl")
                || modelName.contains("gemini") || modelName.contains("gpt-4o");
        boolean tools = true;
        boolean thinking = modelName.contains("thinking") || modelName.contains("r1")
                || modelName.contains("qwq") || modelName.contains("o1") || modelName.contains("o3");
        boolean embedding = false;
        long contextLength = -1;

        return new NaruModelCapabilitiesImpl(vision, tools, thinking, embedding, contextLength);
    }

    static class NaruOpenRouterProtocol extends NaruModelProtocolOpenAICompat {

        NaruOpenRouterProtocol(NaruOpenRouterProvider provider, NaruModelConfig model, String configPrefix, NaruModelCapabilities capabilities) {
            super(provider, model, configPrefix, "chat/completions", capabilities, DEFAULT_BASE_URL);
        }

        @Override
        protected void prepareRequest(NWebRequest request, NElement body, NaruTask task) {
            super.prepareRequest(request, body, task);
            // Recommended attribution headers (optional, configurable)
            task.session().agent().env().get(configPrefix + ".httpReferer").flatMap(x -> x.asStringValue())
                    .ifPresent(v -> request.header("HTTP-Referer", v));
            task.session().agent().env().get(configPrefix + ".xTitle").flatMap(x -> x.asStringValue())
                    .ifPresent(v -> request.header("X-Title", v));
        }
    }
}
