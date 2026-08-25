package net.thevpc.naru.ext.tools.plan;

import net.thevpc.naru.api.model.NaruToolDefinition;
import net.thevpc.naru.api.model.NaruToolDefinitionFunction;
import net.thevpc.naru.api.registry.DefaultNaruTool;
import net.thevpc.naru.api.registry.NaruToolCallContext;
import net.thevpc.naru.api.registry.NaruToolParameter;
import net.thevpc.naru.api.task.NaruTask;

public class PlanGetTool extends DefaultNaruTool {

    public PlanGetTool() {
        super("plan_get", new String[]{});
    }

    @Override
    public String getDescription(NaruTask task) {
        return "Read the current task's execution plan with step statuses. "
                + "Use this to re-check the plan before deciding what to do next.";
    }

    @Override
    public NaruToolDefinition getDefinition(NaruTask task) {
        return new NaruToolDefinitionFunction(
                name(), getDescription(task),
                NaruToolParameter.integer("task_id", "Optional: task id whose plan to read (defaults to current task)", false).build()
        );
    }

    @Override
    public String execute(NaruToolCallContext context) {
        NaruTask task = context.task();
        Integer tid = context.intArg("task_id").orNull();
        long taskId = tid == null ? task.id() : tid;
        return task.session().planManager().activePlan(taskId)
                .map(p -> "Plan:\n" + p.render())
                .orElseGet(() -> "No active plan for task " + taskId);
    }
}
