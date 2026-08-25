package net.thevpc.naru.impl.engine.plan;

import net.thevpc.naru.api.plan.NaruPlan;
import net.thevpc.naru.api.plan.NaruPlanManager;
import net.thevpc.naru.api.plan.NaruPlanStatus;
import net.thevpc.naru.api.plan.NaruPlanStep;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.elem.NElementFormatterStyle;
import net.thevpc.nuts.elem.NObjectElementBuilder;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NOptional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link NaruPlanManager}. Plans live in memory and are persisted
 * as a single TSON file inside the session folder (written by NaruSessionImpl).
 */
public class NaruPlanManagerImpl implements NaruPlanManager {

    private final Map<Long, NaruPlan> plans = new LinkedHashMap<>();

    @Override
    public NOptional<NaruPlan> activePlan(long taskId) {
        NaruPlan p = plans.get(taskId);
        return p == null ? NOptional.ofNamedEmpty(NMsg.ofC("plan of task %s", taskId)) : NOptional.of(p);
    }

    @Override
    public NaruPlan createPlan(long taskId, String goal, List<String> steps) {
        NaruPlan p = new NaruPlan(taskId, goal);
        p.setSteps(steps);
        plans.put(taskId, p);
        return p;
    }

    @Override
    public NOptional<NaruPlan> updateStep(long taskId, int stepId, NaruPlanStatus status, String notes) {
        NaruPlan p = plans.get(taskId);
        if (p == null) {
            return NOptional.ofNamedEmpty(NMsg.ofC("plan of task %s", taskId));
        }
        NaruPlanStep step = null;
        for (NaruPlanStep s : p.steps()) {
            if (s.id() == stepId) {
                step = s;
                break;
            }
        }
        if (step == null) {
            return NOptional.ofNamedError(NMsg.ofC("step %s not found in plan of task %s", stepId, taskId));
        }
        if (status != null) {
            step.setStatus(status);
        }
        if (notes != null) {
            step.setNotes(notes);
        }
        p.touch();
        return NOptional.of(p);
    }

    @Override
    public boolean removePlan(long taskId) {
        return plans.remove(taskId) != null;
    }

    @Override
    public Map<Long, NaruPlan> plans() {
        return new LinkedHashMap<>(plans);
    }

    // ── persistence (invoked by NaruSessionImpl saveFolder/loadFolder) ───────

    public void saveTo(NPath folder) {
        if (plans.isEmpty()) {
            NPath f = folder.resolve("plans.tson");
            if (f.exists()) {
                f.delete();
            }
            return;
        }
        NObjectElementBuilder b = NElement.ofObjectBuilder();
        NArrayElementBuilder arr = NArrayElementBuilder.of();
        for (NaruPlan p : plans.values()) {
            arr.add(p.toElement());
        }
        b.set("plans", arr.build());
        NElementWriter.ofTson().ntf(false).formatter(NElementFormatterStyle.PRETTY)
                .write(b.build(), folder.mkdirs().resolve("plans.tson"));
    }

    public void loadFrom(NPath folder) {
        plans.clear();
        NPath f = folder.resolve("plans.tson");
        if (!f.exists()) {
            return;
        }
        try {
            NElement e = NElementReader.ofTson().ntf(false).read(f);
            e.asObject().flatMap(o -> o.getArray("plans")).ifPresent(arr -> {
                for (NElement el : arr.children()) {
                    NaruPlan p = new NaruPlan(el);
                    plans.put(p.taskId(), p);
                }
            });
        } catch (Exception ex) {
            throw new NIllegalArgumentException(NMsg.ofC("failed to load plans from %s: %s", f, ex.getMessage(), ex));
        }
    }
}
