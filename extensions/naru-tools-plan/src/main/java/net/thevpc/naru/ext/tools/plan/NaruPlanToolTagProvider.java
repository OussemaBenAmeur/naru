package net.thevpc.naru.ext.tools.plan;

import net.thevpc.naru.api.registry.DefaultNaruToolTag;
import net.thevpc.naru.api.registry.NaruToolTag;
import net.thevpc.naru.api.registry.NaruToolTagProvider;
import net.thevpc.naru.api.registry.NaruToolTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NaruPlanToolTagProvider implements NaruToolTagProvider {
    private final List<NaruToolTag> all = new ArrayList<>();

    public NaruPlanToolTagProvider() {
        all.add(new DefaultNaruToolTag(NaruToolTags.PLAN, "Planning tools"));
    }

    @Override
    public String name() {
        return "plan";
    }

    @Override
    public List<NaruToolTag> tags() {
        return Collections.unmodifiableList(all);
    }
}
