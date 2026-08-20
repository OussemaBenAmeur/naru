package net.thevpc.naru.impl.cmdline;

import net.thevpc.naru.api.registry.NaruDirective;
import net.thevpc.naru.impl.engine.stmt.shared.NaruStatementHelper;
import net.thevpc.nuts.cmdline.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.thevpc.naru.api.agent.NaruSession;

public class NaruNArgCompleteResolver implements NArgCompleteResolver {
    private final NaruSession session;

    public NaruNArgCompleteResolver(NaruSession session) {
        this.session = session;
    }

    @Override
    public NArgCompleteResult resolveCandidates(NCmdLine cmdLine, NArgCompletePos pos) {
        List<NArgCompleteCandidate> candidates = new ArrayList<>();
        String[] stringArray = cmdLine.toStringArray();
        int wordIndex = pos.wordIndex();

        if (stringArray.length == 0 || (stringArray.length == 1 && stringArray[0].isEmpty())) {
            // First word - show all directive commands
            for (Map.Entry<String, NaruDirective> e : session.registry().directives().entrySet().stream()
                    .sorted(Comparator.comparing(a -> a.getKey()))
                    .collect(Collectors.toList())) {
                candidates.add(NArgCompleteCandidate.of(
                        "/" + e.getKey(),
                        "/" + e.getKey() + " - " + e.getValue().getDescription()
                ));
            }
            for (String kw : NaruStatementHelper.STATEMENT_KEYWORDS) {
                candidates.add(NArgCompleteCandidate.of(
                        "/" + kw,
                        kw + " - keyword"
                ));
            }
        } else if (wordIndex == 0 && stringArray[0].startsWith("/")) {
            // Command completion - partial match for first command word
            String currentCommand = stringArray[0];
            for (Map.Entry<String, NaruDirective> e : session.registry().directives().entrySet().stream()
                    .sorted(Comparator.comparing(a -> a.getKey()))
                    .collect(Collectors.toList())) {
                String value = "/" + e.getKey();
                if (value.startsWith(currentCommand)) {
                    candidates.add(NArgCompleteCandidate.of(
                            value,
                            value + " - " + e.getValue().getDescription()
                    ));
                }
            }
            for (String kw : NaruStatementHelper.STATEMENT_KEYWORDS) {
                candidates.add(NArgCompleteCandidate.of(
                        "/" + kw,
                        kw + " - keyword"
                ));
            }
        } else if (wordIndex > 0 && stringArray[0].startsWith("/")) {
            // Argument completion for specific commands
            String commandName = stringArray[0].substring(1); // Remove the leading "/"
            NaruDirective directive = session.registry().directives().get(commandName);
            if (directive != null) {
                candidates.addAll(directive.resolveCandidates(cmdLine, pos, session));
            }
        }

        return NArgCompleteResult.ofCandidates(candidates);
    }


}
