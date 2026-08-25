package net.thevpc.naru.ext.tools.plan;

import net.thevpc.naru.api.model.NaruToolDefinition;
import net.thevpc.naru.api.model.NaruToolDefinitionFunction;
import net.thevpc.naru.api.plan.NaruPlan;
import net.thevpc.naru.api.plan.NaruPlanStatus;
import net.thevpc.naru.api.registry.DefaultNaruTool;
import net.thevpc.naru.api.registry.NaruToolCallContext;
import net.thevpc.naru.api.registry.NaruToolParameter;
import net.thevpc.naru.api.task.NaruTask;

public class PlanUpdateTool extends DefaultNaruTool {

    public PlanUpdateTool() {
        super("plan_update", new String[]{});
    }

    @Override
    public String getDescription(NaruTask task) {
        return "Update the status and/or notes of a step in the current task's plan. "
                + "Call this immediately after starting or completing each step.";
    }

    @Override
    public NaruToolDefinition getDefinition(NaruTask task) {
        return new NaruToolDefinitionFunction(
                name(), getDescription(task),
                NaruToolParameter.integer("step", "Step number to update (1-based)", true).build(),
                NaruToolParameter.string("status", "New status: pending, in-progress, completed, blocked", true)
                        .enumValues(java.util.Arrays.asList("pending", "in-progress", "completed", "blocked")).build(),
                NaruToolParameter.string("notes", "Optional progress notes for this step", false).build()
        );
    }

    @Override
    public String execute(NaruToolCallContext context) {
        NaruTask task = context.task();
        Integer stepId = context.intArg("step").orNull();
        String statusStr = context.stringArg("status").onBlankEmpty().orNull();
        String notes = context.stringArg("notes").onBlankEmpty().orNull();
        if (stepId == null || statusStr == null) {
            return "ERROR: step and status are required";
        }
        NaruPlanStatus status = NaruPlanStatus.parse(statusStr).orNull();
        if (status == null) {
            return "ERROR: invalid status '" + statusStr + "' (use pending, in-progress, completed or blocked)";
        }
        return task.session().planManager().updateStep(task.id(), stepId, status, notes)
                .map(p -> "Plan updated:\n" + p.render())
                .orElseGet(() -> "ERROR: no active plan or invalid step");
    }
}
