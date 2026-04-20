package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import me.voidyll.systems.events.Event;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class StopEventCommand extends CommandBase {
    private final EventHandler eventHandler;

    public StopEventCommand(EventHandler eventHandler) {
        super("stop-event", "Stops the currently running event");
        this.eventHandler = eventHandler;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        Event currentEvent = eventHandler.getCurrentEvent();
        
        if (currentEvent != null) {
            String eventId = currentEvent.getEventId();
            eventHandler.stopCurrentEvent();
            context.sendMessage(Message.raw("Stopped event: " + eventId));
        } else {
            context.sendMessage(Message.raw("No event is currently running"));
        }
    }
}
