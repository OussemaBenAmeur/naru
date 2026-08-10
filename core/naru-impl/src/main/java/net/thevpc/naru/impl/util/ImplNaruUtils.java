package net.thevpc.naru.impl.util;

import net.thevpc.naru.api.agent.NaruLogMode;
import net.thevpc.naru.api.task.NaruTask;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.net.NWebRequest;
import net.thevpc.nuts.platform.NStoreScope;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NStringUtils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ImplNaruUtils {

    public static NMsg formatDirective(String name) {
        return NMsg.ofC("%s%s",NMsg.ofStyledSeparator("/"),NMsg.ofStyledPrimary1(name));
    }


    public static boolean isValidRoutineName(String text) {
        if (text == null || text.isEmpty()) return false;

        char first = text.charAt(0);
        if (!Character.isLetter(first) && first != '_') return false;
        if (text.length() == 1) return Character.isLetter(first);
        boolean prevWasDash = false;
        for (int i = 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '-') {
                if (prevWasDash) return false;
                prevWasDash = true;
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                prevWasDash = false;
            } else {
                return false;
            }
        }
        // last char was '-' → prevWasDash is true
        return !prevWasDash;
    }

    public static Map<String, NElement> parseEnv(NElement aa) {
        if (aa == null) {
            return new LinkedHashMap<>();
        }
        Map<String, NElement> env = new HashMap<>();
        if (aa.isNamedPair()) {
            NPairElement p = aa.asPair().get();
            env.put(p.key().asStringValue().orNull(), p.value());
        } else if (aa.isListContainer()) {
            for (NPairElement p : aa.asListContainer().get().namedPairs()) {
                env.put(p.key().asStringValue().orNull(), p.value());
            }
        } else if (aa.isFragment()) {
            for (NPairElement p : aa.asFragment().get().namedPairs()) {
                env.put(p.key().asStringValue().orNull(), p.value());
            }
        }
        return env;
    }


    public static String abbreviate(String s, int max) {
        if (s == null) return "(null)";
        s = s.replace('\n', ' ');
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }


    public static boolean isPath(String command) {
        String a = command.trim();
        if (a.indexOf('/') >= 0) {
            for (int i = 0; i < a.length(); i++) {
                char c = a.charAt(i);
                switch (c) {
                    case ' ':
                    case ':':
                    case '\t':
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case '$':
                    case ',':
                    case ';':
                    case '=':
                    case '#': {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

}
