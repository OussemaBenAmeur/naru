package net.thevpc.naru.api.plan;

import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * Manages durable, task-scoped execution plans.
 * Plans persist with the session (TSON) and survive crashes/reloads.
 */
public interface NaruPlanManager {

    /**
     * The active plan of the given task, if any.
     */
    NOptional<NaruPlan> activePlan(long taskId);

    /**
     * Create (or replace) the plan of the given task from an ordered list of step descriptions.
     */
    NaruPlan createPlan(long taskId, String goal, List<String> steps);

    /**
     * Update a single step's status and/or notes. Returns the updated plan.
     */
    NOptional<NaruPlan> updateStep(long taskId, int stepId, NaruPlanStatus status, String notes);

    /**
     * Remove the plan attached to the given task.
     */
    boolean removePlan(long taskId);

    /**
     * All plans keyed by task id.
     */
    Map<Long, NaruPlan> plans();
}
