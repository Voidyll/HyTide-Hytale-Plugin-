package me.voidyll.systems;

public final class DebugMessageSettings {
    private static volatile boolean debugMessagesEnabled = false;

    private DebugMessageSettings() {
    }

    public static boolean areDebugMessagesEnabled() {
        return debugMessagesEnabled;
    }

    public static boolean toggleDebugMessages() {
        debugMessagesEnabled = !debugMessagesEnabled;
        return debugMessagesEnabled;
    }
}