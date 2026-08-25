package net.thevpc.naru.ext.tools.plan;

import net.thevpc.naru.api.model.NaruToolDefinition;
import net.thevpc.naru.api.model.NaruToolDefinitionFunction;
import net.thevpc.naru.api.plan.NaruPlan;
import net.thevpc.naru.api.registry.DefaultNaruTool;
import net.thevpc.naru.api.registry.NaruToolCallContext;
import net.thevpc.naru.api.registry.NaruToolParameter;
import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.nuts.elem.NElement;

public class PlanCreateTool extends DefaultNaruTool {

    public PlanCreateTool() {
        super("plan_create", new String[]{});
    }

    @Override
    public String getDescription(NaruTask task) {
        return "Create (or replace) the execution plan for the current task. "
                + "Break the work into small, concrete, verifiable steps. "
                + "All steps start as pending. Update each step with plan_update as you progress.";
    }

    @Override
    public NaruToolDefinition getDefinition(NaruTask task) {
        return new NaruToolDefinitionFunction(
                name(), getDescription(task),
                NaruToolParameter.string("goal", "Overall goal of the plan", true).build(),
                NaruToolParameter.array("steps", "Ordered list of step descriptions", true,
                        NaruToolParameter.string("step", "Step description", true).build()).build()
        );
    }

    @Override
    public String execute(NaruToolCallContext context) {
        NaruTask task = context.task();
        String goal = context.stringArg("goal").onBlankEmpty().orNull();
        java.util.List<String> steps = new java.util.ArrayList<>();
        context.arg("steps").ifPresent(v -> {
            NElement e = NElement.of(v);
            if (e.isAnyArray()) {
                for (NElement el : e.asArray().get()) {
                    String s = el.asStringValue().orNull();
                    if (s != null && !s.isBlank()) {
                        steps.add(s.trim());
                    }
                }
            }
        });
        if (goal == null || steps.isEmpty()) {
            return "ERROR: goal and at least one step are required";
        }
        NaruPlan plan = task.session().planManager().createPlan(task.id(), goal, steps);
        return "Plan created:\n" + plan.render();
    }
}
