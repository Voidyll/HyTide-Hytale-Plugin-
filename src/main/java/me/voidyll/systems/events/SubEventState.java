package me.voidyll.systems.events;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks the state of sub-events within an event instance.
 * Thread-safe for concurrent access.
 */
public class SubEventState {
    private final String eventId;
    private final Set<String> executedSubEvents = new HashSet<>();
    private int totalEntitiesKilled = 0;
    private static final int MAX_SUB_EVENTS_PER_EVENT = 100; // Safety limit to prevent infinite loops
    
    public SubEventState(String eventId) {
        this.eventId = eventId;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    /**
     * Mark a sub-event as executed.
     * @return true if marked, false if already executed or limit reached
     */
    public synchronized boolean markSubEventExecuted(String subEventId) {
        if (subEventId == null) {
            return false;
        }
        
        if (executedSubEvents.size() >= MAX_SUB_EVENTS_PER_EVENT) {
            System.err.println("[SubEventState:" + eventId + "] Maximum sub-event execution limit reached (" + MAX_SUB_EVENTS_PER_EVENT + ")");
            return false;
        }
        
        return executedSubEvents.add(subEventId);
    }
    
    /**
     * Check if a sub-event has been executed.
     */
    public synchronized boolean hasSubEventExecuted(String subEventId) {
        if (subEventId == null) {
            return false;
        }
        return executedSubEvents.contains(subEventId);
    }
    
    /**
     * Increment the entity kill counter.
     */
    public synchronized void addEntityKill() {
        totalEntitiesKilled++;
    }
    
    /**
     * Get total entities killed since event start.
     */
    public synchronized int getTotalEntitiesKilled() {
        return totalEntitiesKilled;
    }
    
    /**
     * Get the number of executed sub-events (for debugging).
     */
    public synchronized int getExecutedCount() {
        return executedSubEvents.size();
    }
}
