package net.thevpc.naru.api.plan;

import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A durable, ordered execution plan attached to a task.
 * Plans persist with the session and are injected into the LLM prompt
 * while active.
 */
public class NaruPlan {
    private final long taskId;
    private String goal;
    private Instant creationInstant;
    private Instant modificationInstant;
    private final List<NaruPlanStep> steps = new ArrayList<>();

    public NaruPlan(long taskId, String goal) {
        this.taskId = taskId;
        this.goal = goal;
        this.creationInstant = Instant.now();
        this.modificationInstant = creationInstant;
    }

    public NaruPlan(NElement element) {
        NObjectElement o = element.asObject().get();
        this.taskId = o.getLongValue("taskId").orElse(0L);
        this.goal = o.getStringValue("goal").orNull();
        this.creationInstant = o.getInstantValue("creationInstant").orElse(Instant.now());
        this.modificationInstant = o.getInstantValue("modificationInstant").orElse(creationInstant);
        o.getArray("steps").ifPresent(arr -> {
            for (NElement e : arr.children()) {
                steps.add(new NaruPlanStep(e));
            }
        });
    }

    public long taskId() {
        return taskId;
    }

    public String goal() {
        return goal;
    }

    public NaruPlan setGoal(String goal) {
        this.goal = goal;
        return this;
    }

    public Instant creationInstant() {
        return creationInstant;
    }

    public Instant modificationInstant() {
        return modificationInstant;
    }

    public List<NaruPlanStep> steps() {
        return Collections.unmodifiableList(steps);
    }

    public NaruPlan setSteps(List<String> descriptions) {
        steps.clear();
        int id = 1;
        for (String d : descriptions) {
            if (d != null && !d.isBlank()) {
                steps.add(new NaruPlanStep(id++, d.trim()));
            }
        }
        touch();
        return this;
    }

    public NaruPlanStep step(int stepId) {
        for (NaruPlanStep s : steps) {
            if (s.id() == stepId) {
                return s;
            }
        }
        return null;
    }

    public boolean isComplete() {
        if (steps.isEmpty()) {
            return false;
        }
        for (NaruPlanStep s : steps) {
            if (s.status() != NaruPlanStatus.COMPLETED && s.status() != NaruPlanStatus.BLOCKED) {
                return false;
            }
        }
        return true;
    }

    public NaruPlan touch() {
        this.modificationInstant = Instant.now();
        return this;
    }

    /**
     * Compact rendering used for prompt injection and /plan show.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Goal: ").append(goal).append('\n');
        for (NaruPlanStep s : steps) {
            sb.append("  ").append(s.id()).append(". [")
                    .append(s.status().name().toLowerCase().replace('_', '-'))
                    .append("] ").append(s.description());
            if (s.notes() != null) {
                sb.append(" -- ").append(s.notes());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public NElement toElement() {
        NArrayElementBuilder stepsArr = NArrayElementBuilder.of();
        for (NaruPlanStep s : steps) {
            stepsArr.add(s.toElement());
        }
        return NElement.ofObjectBuilder()
                .set("taskId", taskId)
                .set("goal", goal)
                .set("creationInstant", NElement.ofInstant(creationInstant))
                .set("modificationInstant", NElement.ofInstant(modificationInstant))
                .set("steps", stepsArr.build())
                .build();
    }
}
