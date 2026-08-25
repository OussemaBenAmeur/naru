package net.thevpc.naru.ext.models.openapi;

import net.thevpc.naru.api.model.*;
import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.naru.ext.models.NaruModelProtocolBase;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.net.NWebCli;
import net.thevpc.nuts.net.NWebRequest;
import net.thevpc.nuts.net.NWebResponse;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.Map;

public class NaruModelProtocolOpenAICompat extends NaruModelProtocolBase {

    /**
     * Fallback base url used when config key {@code <configPrefix>.url} is not set.
     */
    protected final String defaultBaseUrl;

    public NaruModelProtocolOpenAICompat(NaruModelProvider provider, NaruModelConfig model, String configPrefix, String chatPath, NaruModelCapabilities capabilities) {
        this(provider, model, configPrefix, chatPath, capabilities, null);
    }

    public NaruModelProtocolOpenAICompat(NaruModelProvider provider, NaruModelConfig model, String configPrefix, String chatPath, NaruModelCapabilities capabilities, String defaultBaseUrl) {
        super(provider, model, configPrefix, chatPath, capabilities,
                new NaruOpenApiRequestSerializer(),
                new NaruOpenApiResponseParser()
        );
        this.defaultBaseUrl = defaultBaseUrl;
    }

    @Override
    public String url(NaruTask task, Map<String, NElement> env) {
        if (defaultBaseUrl != null) {
            return task.session().agent().env().get(configPrefix + ".url")
                    .flatMap(x -> x.asStringValue())
                    .map(x -> x.replaceAll("/$", ""))
                    .orElse(defaultBaseUrl);
        }
        return super.url(task, env);
    }

    protected String apiKeyConfigKey() {
        return configPrefix + ".apiKey";
    }

    protected String apiKey(NaruTask task) {
        return task.session().agent().env().get(apiKeyConfigKey())
                .flatMap(x -> x.asStringValue()).orNull();
    }

    protected void prepareRequest(NWebRequest request, NElement body, NaruTask task) {
        String apiKey = apiKey(task);
        if (!NBlankable.isBlank(apiKey)) {
            request.header("Authorization", "Bearer " + apiKey);
        }
    }

    @Override
    public NaruResponse chat(NaruModelRequest naruModelRequest, NaruTask task) {
        NElement body = serializer.serialize(naruModelRequest, model, task.session());
        Map<String, NElement> env = naruModelRequest.env();
        NWebCli http = NWebCli.of()
                .connectTimeout(connectTimeout(task, env))
                .baseUri(url(task, env));
        NWebRequest request = http.POST(chatPath)
                .timeout(readTimeout(task, env))
                .jsonRequestBody(body);
        prepareRequest(request, body, task);

        String responseString = null;
        try {
            NWebResponse response = request.run().ifErrorThrow();
            responseString = response.contentAsString();
            return parseResponse(responseString);
        } catch (Exception e) {
            NLog.of(getClass())
                    .log(
                            NMsg.ofC("Failed to communicate with %s at %s: %s\n-----BODY\n%s\n-----BODY\n-----RESPONSE\n%s\n-----RESPONSE", provider().name(), request.effectiveUri(), e.getMessage(), e,
                                    NElementWriter.ofJson().formatPlain(body),
                                    responseString
                            ).asError()
                    );
            throw new NIllegalArgumentException(
                    NMsg.ofC("Failed to communicate with %s at %s: %s", provider().name(), request.effectiveUri(), e.getMessage(), e)
            );
        }
    }

}
