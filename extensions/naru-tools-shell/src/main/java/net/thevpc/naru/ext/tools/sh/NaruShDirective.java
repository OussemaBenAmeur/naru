package net.thevpc.naru.ext.tools.sh;

import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.naru.api.registry.NaruDirectiveCallContext;
import net.thevpc.naru.api.registry.NaruDirectiveBase;
import net.thevpc.naru.api.util.NaruUtils;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NAnsiTermHelper;
import net.thevpc.nuts.text.NMsg;

import java.util.logging.Level;

public class NaruShDirective extends NaruDirectiveBase {
    public NaruShDirective() {
        super("sh","general", "run shell command");
        register(new AbstractSubCommand() {
            @Override
            public void execute(NaruDirectiveCallContext context, NCmdLine cmdLine) {
                NaruTask task = context.task();
                try (NSession session = NSession.of().copy()) {
                    session.setLogTermLevel(Level.OFF);
                    session.runWith(() -> {
                        NExec e = NExec.of("nsh","--progress=none", "-c", context.argument()).directory(task.workingDir()).failFast(false);
                        String result = e
                                .grabbedAll();
                        task.addResultMessage(
                                NMsg.ofC("call   : nsh -c %s\nexit code %s\nresult : \n%s", context.argument(),e.exitCode(), NAnsiTermHelper.of().stripAnsi(result))
                                        .withLevel(e.exitCode()!=0?Level.SEVERE : Level.INFO)
                        );
                    });
                }
            }
        });
    }

}
