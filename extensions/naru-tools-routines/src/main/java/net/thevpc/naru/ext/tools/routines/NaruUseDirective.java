package net.thevpc.naru.ext.tools.routines;

import net.thevpc.naru.api.registry.NaruDirectiveCallContext;
import net.thevpc.naru.api.registry.NaruDirectiveBase;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NMsg;

public class NaruUseDirective extends NaruDirectiveBase {
    public NaruUseDirective() {
        super("use", "routine", "use a routine");
        register(new AbstractSubCommand(
                new SubCommandHelp("<routine>", "use routine by name")
        ) {
            @Override
            public void execute(NaruDirectiveCallContext context, NCmdLine cmdLine) {
                NArg n = cmdLine.next().orNull();
                if(n==null || n.isOption()){
                    context.task().throwError(NMsg.ofC("expected <routine-name>"));
                    return;
                }
                context.task().frame().editRoutine(n.image());
            }
        });
    }

}
