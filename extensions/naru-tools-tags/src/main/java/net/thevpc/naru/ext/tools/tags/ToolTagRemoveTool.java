package net.thevpc.naru.ext.tools.tags;

import net.thevpc.naru.api.model.NaruToolDefinition;
import net.thevpc.naru.api.model.NaruToolDefinitionFunction;
import net.thevpc.naru.api.registry.NaruTool;
import net.thevpc.naru.api.registry.NaruToolCallContext;
import net.thevpc.naru.api.registry.NaruToolParameter;
import net.thevpc.naru.api.registry.NaruToolTag;
import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.nuts.util.NStringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ToolTagRemoveTool implements NaruTool {

    @Override
    public String name() {
        return "tag_remove";
    }

    @Override
    public String getDescription(NaruTask task) {
        return "Removes tools that define the given tag";
    }

    // empty set, it is always included
    @Override
    public Set<String> tags() {
        return Set.of();
    }

    public boolean isRelevant(NaruTask task){
        return !task.findToolTags().isEmpty();
    }

    @Override
    public NaruToolDefinition getDefinition(NaruTask task) {
        List<NaruToolTag> alreadyAdded = task.findToolTags();
        StringBuilder sb = new StringBuilder();
        if(alreadyAdded.isEmpty()){
            sb.append("remove one or more tags by name (comma separated), but it seems no tag has already been added yet.");
        }else{
            sb.append("remove one or more tags by name (comma separated) from the following list :");
            for (NaruToolTag value : alreadyAdded) {
                sb.append("\n        ").append(value.name()).append(" : ").append(value.description());
            }
        }
        return new NaruToolDefinitionFunction(
                name(),
                sb.toString(),
                NaruToolParameter.string("tags", sb.toString(), true).build()
        );
    }

    @Override
    public String execute(NaruToolCallContext context) {
        String t = context.stringArg("tags").orNull();
        Set<String> added = new LinkedHashSet<>();
        if (t != null) {
            for (String s : NStringUtils.split(t, ", ;", true, true)) {
                context.task().removeToolTag(s);
                added.add(s);
            }
        }
        return "removed " + added.size() + " tags";
    }
}
