package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DeleteEventCommand extends CommandBase {
    private final RequiredArg<String> EVENT_ID;
    private final EventHandler eventHandler;

    public DeleteEventCommand(EventHandler eventHandler) {
        super("delete-event", "Deletes an event by ID");
        this.eventHandler = eventHandler;
        this.EVENT_ID = withRequiredArg("eventId", "The ID of the event to delete", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String eventId = EVENT_ID.get(context);

        if (eventHandler.removeEvent(eventId)) {
            context.sendMessage(Message.raw("Event '" + eventId + "' deleted."));
        } else {
            context.sendMessage(Message.raw("Event '" + eventId + "' not found."));
        }
    }
}
