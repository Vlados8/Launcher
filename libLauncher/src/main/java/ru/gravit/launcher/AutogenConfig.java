package ru.gravit.launcher;

public class AutogenConfig {
    public String projectname;
    public String address;
    public int port;
    private boolean isInitModules;
    public String secretKeyClient;

    AutogenConfig() {

    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void initModules() {
        if (isInitModules) return;
    }
}
