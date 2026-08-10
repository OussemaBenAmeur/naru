package net.thevpc.naru.ext.tools.llm;

import net.thevpc.naru.api.agent.NaruSource;
import net.thevpc.naru.api.model.*;
import net.thevpc.naru.api.registry.NaruToolCallContext;
import net.thevpc.naru.api.registry.NaruToolParameter;
import net.thevpc.naru.api.registry.NaruToolTags;
import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.naru.api.registry.DefaultNaruTool;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModelDelegateTool extends DefaultNaruTool {

    public ModelDelegateTool() {
        super("delegate_to_model", new String[]{NaruToolTags.AI});
    }


    @Override
    public String name() {
        return "delegate_to_model";
    }

    private String availableModelsDescription(NaruTask task) {
        List<NaruModelInfo> models = task.session().registry().modelsInfos(task.session());
        if (models.isEmpty()) {
            return "Unknown (try any model name)";
        } else {
            return String.join(", ", models.toString());
        }

    }

    @Override
    public String getDescription(NaruTask task) {
        return "Delegate a sub-task to another AI model. Use this to offload vision tasks to vision models, or complex reasoning to larger models. Available models: "
                + availableModelsDescription(task);
    }

    @Override
    public NaruToolDefinition getDefinition(NaruTask task) {
        return new NaruToolDefinitionFunction(
                name(),
                getDescription(task),
                NaruToolParameter.string("model_name", "The exact name of the model to use.", true).build(),
                NaruToolParameter.string("prompt", "The task description or question for the model.", true).build(),
                NaruToolParameter.string("image_path", "Optional absolute path to an image file if this is a vision task.", false).build()
        );
    }

    @Override
    public String execute(NaruToolCallContext context) {
        return callModel(context.task()
                ,context.stringArg("model_name").orNull()
                ,context.stringArg("prompt").orNull()
                ,context.stringArg("image_path").orNull()
        );
    }

    public static String callModel(NaruTask task, String modelName, String prompt, String imagePath) {

        if (NBlankable.isBlank(modelName)) return "Error: model_name is required.";
        if (NBlankable.isBlank(prompt)) return "Error: prompt is required.";

        List<NaruMessage> messages = new ArrayList<>();
        NaruMessage msg = NaruMessage.user(prompt);

        if (!NBlankable.isBlank(imagePath)) {
            try {
                String base64 = ImageUtil.toBase64(task.resolve(imagePath).toString());
                msg.setImages(Collections.singletonList(base64));
            } catch (Exception e) {
                return "Error loading image: " + e.getMessage();
            }
        }


        messages.add(msg);
        NaruModelConfig model = task.session().findModel(modelName).orNull();
        if (model == null) {
            return "Error: Model not found : " + modelName;
        }
        NaruModelConfig oldModel = task.model();
        task.setModel(model);
        Map<String, NElement> env = task.context(NaruSource.values()).env();
        try {
            NaruResponse response = task.chat(model,
                    new NaruModelRequest(messages,
                            env
                    )
            );
            if (response.getMessage() != null) {
                return response.getMessage().getContent();
            }
            return "Error: Model returned empty response.";
        } catch (Exception e) {
            return "Error calling model " + modelName + ": " + e.getMessage();
        } finally {
            task.setModel(oldModel);
        }
    }
}
