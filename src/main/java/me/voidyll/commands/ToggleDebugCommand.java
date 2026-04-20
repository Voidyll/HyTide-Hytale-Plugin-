package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.DebugMessageSettings;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ToggleDebugCommand extends CommandBase {

    public ToggleDebugCommand() {
        super("toggle-debug", "Toggles debug messages sent to players");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        boolean enabled = DebugMessageSettings.toggleDebugMessages();
        context.sendMessage(Message.raw(enabled
            ? "Debug messages are now enabled."
            : "Debug messages are now disabled."));
    }
}