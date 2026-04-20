package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import me.voidyll.systems.events.Event;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Map;

public class ListEventsCommand extends CommandBase {
    private final EventHandler eventHandler;

    public ListEventsCommand(EventHandler eventHandler) {
        super("list-events", "Lists all available events and their status");
        this.eventHandler = eventHandler;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        Map<String, Event> events = eventHandler.getAllEvents();
        Event currentEvent = eventHandler.getCurrentEvent();
        
        if (events.isEmpty()) {
            context.sendMessage(Message.raw("No events available"));
            return;
        }
        
        context.sendMessage(Message.raw("=== Events ==="));
        
        for (Map.Entry<String, Event> entry : events.entrySet()) {
            String eventId = entry.getKey();
            boolean hasStarted = eventHandler.hasEventBeenStarted(eventId);
            boolean isOngoing = currentEvent != null && currentEvent.getEventId().equals(eventId);
            
            String status;
            if (isOngoing) {
                long elapsedSeconds = currentEvent.getElapsedTimeMs() / 1000;
                status = "[ONGOING - " + elapsedSeconds + "s]";
            } else if (hasStarted) {
                status = "[Started]";
            } else {
                status = "[Not Started]";
            }
            
            context.sendMessage(Message.raw(" " + eventId + " " + status));
        }
    }
}
