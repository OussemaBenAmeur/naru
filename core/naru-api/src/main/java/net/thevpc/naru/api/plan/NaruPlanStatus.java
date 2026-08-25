package net.thevpc.naru.api.plan;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

public enum NaruPlanStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED;

    public static NOptional<NaruPlanStatus> parse(String s) {
        if (s == null || s.isBlank()) {
            return NOptional.ofNamedEmpty("plan status");
        }
        try {
            return NOptional.of(NaruPlanStatus.valueOf(s.trim().toUpperCase().replace('-', '_')));
        } catch (IllegalArgumentException e) {
            return NOptional.ofNamedError(NMsg.ofC("invalid plan status '%s'", s));
        }
    }
}
