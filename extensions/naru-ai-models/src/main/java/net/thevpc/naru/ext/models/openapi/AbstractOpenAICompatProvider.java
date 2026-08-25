package net.thevpc.naru.ext.models.openapi;

import net.thevpc.naru.api.agent.NaruSession;
import net.thevpc.naru.api.model.AbstractNaruModelProvider;
import net.thevpc.naru.api.model.NaruModelCapabilities;
import net.thevpc.naru.api.model.NaruModelConfig;
import net.thevpc.naru.api.model.NaruModelProtocol;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.*;

/**
 * Base class for OpenAI-compatible cloud providers (Groq, Cerebras, OpenRouter, GitHub Models, ...).
 * Subclasses provide a name, a default base url, a model list and static capabilities;
 * the wire protocol and auth handling are inherited.
 */
public abstract class AbstractOpenAICompatProvider extends AbstractNaruModelProvider {

    protected final Map<NaruModelConfig, NaruModelProtocol> protocols = new HashMap<>();
    protected final List<String> supportedModels = new ArrayList<>();

    protected AbstractOpenAICompatProvider(String name) {
        super(name);
    }

    protected String chatPath() {
        return "chat/completions";
    }

    /**
     * Fallback base url when {@code <name>.url} config is not set.
     */
    protected abstract String defaultBaseUrl();

    /**
     * Statically maps capabilities since cloud-hosted capabilities cannot be polled natively.
     */
    protected abstract NaruModelCapabilities resolveCapabilities(String modelName);

    protected NaruModelProtocol createProtocol(NaruModelConfig model, NaruModelCapabilities capabilities) {
        return new NaruModelProtocolOpenAICompat(this, model, name(), chatPath(), capabilities, defaultBaseUrl());
    }

    @Override
    public NOptional<NaruModelProtocol> getProtocol(NaruModelConfig model, NaruSession session) {
        if (!model.provider().equals(name())) {
            return NOptional.ofNamedEmpty(NMsg.ofC("protocol for %s", model));
        }
        NaruModelCapabilities capabilities = resolveCapabilities(model.model());
        return NOptional.of(protocols.computeIfAbsent(model,
                k -> createProtocol(model, capabilities)
        ));
    }

    protected String apiKeyConfigKey() {
        return name() + ".apiKey";
    }

    @Override
    public List<String> findModelIds(NaruSession session) {
        String apiKey = session.agent().env().get(apiKeyConfigKey())
                .flatMap(x -> x.asStringValue())
                .orNull();
        if (NBlankable.isBlank(apiKey)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(supportedModels);
    }
}
