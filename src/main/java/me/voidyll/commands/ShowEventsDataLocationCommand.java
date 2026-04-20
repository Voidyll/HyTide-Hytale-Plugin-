package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ShowEventsDataLocationCommand extends CommandBase {
    private final EventHandler eventHandler;

    public ShowEventsDataLocationCommand(EventHandler eventHandler) {
        super("show-events-data-location", "Shows the file path for events data");
        this.eventHandler = eventHandler;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String filePath = eventHandler.getConfigFilePath().toAbsolutePath().toString();
        
        context.sendMessage(Message.raw("Events data file location:"));
        context.sendMessage(Message.raw("%s".formatted(filePath)));
        context.sendMessage(Message.raw("You can edit this file to manually change event configurations."));
        context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
    }
}
