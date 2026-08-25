package net.thevpc.naru.api.agent;

public enum NaruSource {
    SYSTEM, PROJECT, FOLDER, USER, CLASSPATH, USER_HOME, WORKSPACE, SKILL, MODE, PLAN, ASSISTANT, AGENT;

    public String id() {
        return name().toLowerCase();
    }
}
