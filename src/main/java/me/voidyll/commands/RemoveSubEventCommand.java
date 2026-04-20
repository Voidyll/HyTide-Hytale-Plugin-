package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RemoveSubEventCommand extends CommandBase {
    private final EventHandler eventHandler;
    private final RequiredArg<String> EVENT_ID;
    private final RequiredArg<String> SUB_EVENT_ID;

    public RemoveSubEventCommand(EventHandler eventHandler) {
        super("remove-sub-event", "Remove a sub-event from an existing event");
        this.eventHandler = eventHandler;
        this.EVENT_ID = withRequiredArg("eventId", "ID of the parent event", ArgTypes.STRING);
        this.SUB_EVENT_ID = withRequiredArg("subEventId", "ID of the sub-event to remove", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String eventId = EVENT_ID.get(context);
        String subEventId = SUB_EVENT_ID.get(context);

        boolean success = eventHandler.removeSubEvent(eventId, subEventId);
        if (success) {
            context.sendMessage(Message.raw("Sub-event '" + subEventId + "' removed from event '" + eventId + "'"));
        } else {
            context.sendMessage(Message.raw("Could not remove: event '" + eventId + "' not found or sub-event '" + subEventId + "' does not exist."));
        }
    }
}
