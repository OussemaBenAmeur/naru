package net.thevpc.naru.api.plan;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;

/**
 * A single step within a {@link NaruPlan}.
 */
public class NaruPlanStep {
    private final int id;
    private String description;
    private NaruPlanStatus status;
    private String notes;

    public NaruPlanStep(int id, String description) {
        this(id, description, NaruPlanStatus.PENDING, null);
    }

    public NaruPlanStep(int id, String description, NaruPlanStatus status, String notes) {
        this.id = id;
        this.description = description;
        this.status = status == null ? NaruPlanStatus.PENDING : status;
        this.notes = notes;
    }

    public NaruPlanStep(NElement element) {
        NObjectElement o = element.asObject().get();
        this.id = o.getIntValue("id").orElse(0);
        this.description = o.getStringValue("description").orNull();
        this.status = NaruPlanStatus.parse(o.getStringValue("status").orNull()).orElse(NaruPlanStatus.PENDING);
        this.notes = o.getStringValue("notes").orNull();
    }

    public int id() {
        return id;
    }

    public String description() {
        return description;
    }

    public NaruPlanStep setDescription(String description) {
        this.description = description;
        return this;
    }

    public NaruPlanStatus status() {
        return status;
    }

    public NaruPlanStep setStatus(NaruPlanStatus status) {
        this.status = status == null ? NaruPlanStatus.PENDING : status;
        return this;
    }

    public String notes() {
        return notes;
    }

    public NaruPlanStep setNotes(String notes) {
        this.notes = (notes == null || notes.isBlank()) ? null : notes;
        return this;
    }

    public NElement toElement() {
        return NElement.ofObjectBuilder()
                .set("id", id)
                .set("description", description)
                .set("status", status.name().toLowerCase())
                .set("notes", notes)
                .build();
    }
}
