package me.voidyll.systems.events;

import com.hypixel.hytale.server.core.universe.world.World;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for all events.
 * Events can modify spawn director behavior and execute custom logic.
 */
public abstract class Event {
    protected final String eventId;
    protected final EventConfig config;
    protected boolean isRunning = false;
    protected long startTimeMs = 0;
    
    // Reference to EventHandler for tracking purposes (optional)
    protected Object eventHandler;
    
    // End condition tracking
    protected Map<String, Integer> entityKillCounts = new HashMap<>();
    protected int blocksBrokenCount = 0;
    protected boolean endConditionMet = false;
    
    // Sub-event tracking
    protected SubEventState subEventState;
    protected long lastSubEventCheckMs = 0;
    
    public Event(String eventId, EventConfig config) {
        this.eventId = eventId;
        this.config = config;
    }
    
    /**
     * Set the event handler reference for tracking purposes.
     * @param eventHandler The EventHandler instance
     */
    public void setEventHandler(Object eventHandler) {
        this.eventHandler = eventHandler;
    }
    
    /**
     * Called when the event starts.
     * Override to add custom initialization logic.
     */
    public void onStart(World world) {
        this.isRunning = true;
        this.startTimeMs = System.currentTimeMillis();
        this.blocksBrokenCount = 0;
        this.entityKillCounts.clear();
        this.endConditionMet = false;
        this.lastSubEventCheckMs = System.currentTimeMillis();
        
        // Initialize sub-events
        this.subEventState = new SubEventState(eventId);
        parseSubEventsTypes();
        
        // Debug: log sub-event initialization
        List<SubEvent> loadedSubEvents = config.getSubEvents();
        System.out.println("[Event:" + eventId + "] onStart: subEventState initialized, found " + 
            (loadedSubEvents != null ? loadedSubEvents.size() : 0) + " sub-events");
        if (loadedSubEvents != null) {
            for (SubEvent se : loadedSubEvents) {
                System.out.println("[Event:" + eventId + "]   Sub-event: id=" + se.getId() + 
                    ", trigger=" + (se.getTrigger() != null ? se.getTrigger().getTriggerType() : "null") +
                    ", actions=" + (se.getActions() != null ? se.getActions().size() : 0));
            }
        }
    }
    
    /**
     * Called every tick while the event is running.
     * Override to add custom per-tick logic.
     * 
     * @param deltaMs Time since last tick in milliseconds
     * @param world The game world
     */
    public void onTick(long deltaMs, World world) {
        // Check and execute sub-events
        checkSubEventTriggers(world);
    }
    
    // Debug counter to limit tick logging
    private transient int tickLogCounter = 0;
    
    /**
     * Called when the event ends (either by condition or forced stop).
     * Override to add custom cleanup logic.
     */
    public void onEnd(World world) {
        this.isRunning = false;
    }
    
    /**
     * Check if the event's end condition has been met.
     * 
     * @return true if event should end
     */
    public boolean checkEndCondition() {
        if (endConditionMet) {
            return true;
        }
        
        switch (config.getEndConditionType()) {
            case TIMER:
                long elapsed = System.currentTimeMillis() - startTimeMs;
                return elapsed >= config.getTimerDurationMs();
                
            case TRIGGER_ZONE:
                // Checked externally via notifyTriggerZoneActivated
                return false;
                
            case BLOCK_BREAK:
                return blocksBrokenCount >= config.getBlockBreakCount();
                
            case ENTITY_KILLED:
                // Check if all required entity kills have been met
                return checkAllEntityKillRequirementsMet();
                
            case BLOCK_INTERACTION:
                // Checked externally via notifyBlockInteraction
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * Check if all entity kill requirements have been met.
     * Requirements are specified in config as JSON object: { "HyTide_Shadow_Knight": 2, "HyTide_Rex_Cave": 1 }
     */
    private boolean checkAllEntityKillRequirementsMet() {
        com.google.gson.JsonObject requirements = config.getEntityKillRequirements();
        
        if (requirements == null || requirements.entrySet().isEmpty()) {
            return false;
        }
        
        for (String entityRole : requirements.keySet()) {
            int required = requirements.get(entityRole).getAsInt();
            int current = entityKillCounts.getOrDefault(entityRole, 0);
            
            if (current < required) {
                return false;
            }
        }
        
        return true;
    }
    
    // ==================== External Notifications ====================
    
    /**
     * Notify event that a trigger zone was activated.
     */
    public void notifyTriggerZoneActivated(String zoneName) {
        if (config.getEndConditionType() == EventEndConditionType.TRIGGER_ZONE) {
            if (zoneName.equals(config.getTriggerZoneName())) {
                endConditionMet = true;
            }
        }
    }
    
    /**
     * Notify event that a block was broken.
     */
    public void notifyBlockBroken(String blockType) {
        if (config.getEndConditionType() == EventEndConditionType.BLOCK_BREAK) {
            if (blockType.equals(config.getBlockBreakType())) {
                blocksBrokenCount++;
            }
        }
    }
    
    /**
     * Notify event that an entity was killed.
     * Tracks kills per entity role for multi-entity end conditions.
     */
    public void notifyEntityKilled(String entityRole) {
        if (config.getEndConditionType() == EventEndConditionType.ENTITY_KILLED) {
            // Track kills for this entity type
            entityKillCounts.put(entityRole, entityKillCounts.getOrDefault(entityRole, 0) + 1);
        }
        
        // Notify sub-events of entity kill
        if (subEventState != null) {
            subEventState.addEntityKill();
        }
    }
    
    /**
     * Get current kill count for a specific entity role.
     */
    public int getEntityKillCount(String entityRole) {
        return entityKillCounts.getOrDefault(entityRole, 0);
    }
    
    /**
     * Get all entity kill counts as a map.
     */
    public Map<String, Integer> getAllEntityKillCounts() {
        return new HashMap<>(entityKillCounts);
    }
    
    /**
     * Notify event that a block was interacted with.
     */
    public void notifyBlockInteraction(String blockType) {
        if (config.getEndConditionType() == EventEndConditionType.BLOCK_INTERACTION) {
            if (blockType.equals(config.getBlockInteractionType())) {
                endConditionMet = true;
            }
        }
        
        // Notify sub-events of block interaction
        if (subEventState != null) {
            notifySubEventsOfBlockInteraction(blockType);
        }
    }
    
    // ==================== Sub-Event Methods ====================
    
    /**
     * Parse the types of all sub-events after loading from JSON.
     */
    private void parseSubEventsTypes() {
        List<SubEvent> subEvents = config.getSubEvents();
        for (SubEvent subEvent : subEvents) {
            subEvent.parseTypes();
        }
    }
    
    /**
     * Check all sub-event triggers and execute eligible ones.
     */
    private void checkSubEventTriggers(World world) {
        if (subEventState == null) {
            return;
        }
        
        List<SubEvent> subEvents = config.getSubEvents();
        if (subEvents == null || subEvents.isEmpty()) {
            return;
        }
        
        long currentTimeMs = System.currentTimeMillis();
        long elapsedSinceEventStart = currentTimeMs - startTimeMs;
        
        // Debug: log every ~100 ticks (roughly every few seconds)
        tickLogCounter++;
        if (tickLogCounter % 100 == 1) {
            System.out.println("[Event:" + eventId + "] checkSubEventTriggers: elapsed=" + elapsedSinceEventStart + "ms, subEvents=" + subEvents.size() + ", subEventState=" + (subEventState != null));
        }
        
        // Create a snapshot of sub-events to avoid concurrent modification
        List<SubEvent> subEventsSnapshot = new java.util.ArrayList<>(subEvents);
        
        for (SubEvent subEvent : subEventsSnapshot) {
            if (subEvent == null) {
                continue;
            }
            
            String subEventId = subEvent.getId();
            if (subEventId == null) {
                System.err.println("[Event:"+eventId+"] Sub-event has null ID, skipping");
                continue;
            }
            
            // Skip if already executed (one-time per event instance)
            if (subEventState.hasSubEventExecuted(subEventId)) {
                continue;
            }
            
            SubEventTrigger trigger = subEvent.getTrigger();
            if (trigger == null) {
                continue;
            }
            
            boolean shouldExecute = false;
            
            try {
                switch (trigger.getTriggerType()) {
                    case TIMER:
                        // Execute after delay time has passed
                        long delayMs = trigger.getTimerDelayMs();
                        if (elapsedSinceEventStart >= delayMs) {
                            shouldExecute = true;
                        }
                        break;
                        
                    case ENTITY_KILLED:
                        // Execute after threshold number of entities killed since event start
                        int threshold = trigger.getEntityKillThreshold();
                        if (subEventState.getTotalEntitiesKilled() >= threshold) {
                            shouldExecute = true;
                        }
                        break;
                        
                    case SUB_EVENT_COMPLETION:
                        // Execute when parent sub-event completes
                        String parentSubEventId = trigger.getParentSubEventId();
                        if (parentSubEventId != null && !parentSubEventId.isEmpty() && 
                            subEventState.hasSubEventExecuted(parentSubEventId)) {
                            shouldExecute = true;
                        }
                        break;
                        
                    // BLOCK_INTERACTION is checked externally via notifySubEventsOfBlockInteraction
                }
            } catch (Exception e) {
                System.err.println("[Event:"+eventId+"] Error checking trigger for sub-event: " + subEventId);
                e.printStackTrace();
                continue;
            }
            
            // Debug: log trigger evaluation
            if (tickLogCounter % 100 == 1) {
                System.out.println("[Event:" + eventId + "]   Sub-event '" + subEventId + "': triggerType=" + trigger.getTriggerType() + ", shouldExecute=" + shouldExecute + ", elapsed=" + elapsedSinceEventStart + "ms");
            }
            
            if (shouldExecute) {
                System.out.println("[Event:" + eventId + "] >>> EXECUTING sub-event '" + subEventId + "' at elapsed=" + elapsedSinceEventStart + "ms");
                executeSubEvent(subEvent, world);
            }
        }
    }
    
    /**
     * Notify sub-events of block interaction and execute any that are triggered by it.
     */
    private void notifySubEventsOfBlockInteraction(String blockType) {
        if (subEventState == null || blockType == null) {
            return;
        }
        
        World world = null;
        try {
            world = com.hypixel.hytale.server.core.universe.Universe.get().getDefaultWorld();
        } catch (Exception e) {
            System.err.println("[Event:"+eventId+"] Failed to get world for sub-event execution");
            e.printStackTrace();
            return;
        }
        
        if (world == null) {
            return;
        }
        
        List<SubEvent> subEvents = config.getSubEvents();
        if (subEvents == null || subEvents.isEmpty()) {
            return;
        }
        
        // Create snapshot to avoid concurrent modification
        List<SubEvent> subEventsSnapshot = new java.util.ArrayList<>(subEvents);
        for (SubEvent subEvent : subEventsSnapshot) {
            if (subEvent == null) {
                continue;
            }
            
            String subEventId = subEvent.getId();
            if (subEventId == null) {
                continue;
            }
            
            // Skip if already executed
            if (subEventState.hasSubEventExecuted(subEventId)) {
                continue;
            }
            
            SubEventTrigger trigger = subEvent.getTrigger();
            if (trigger == null || trigger.getTriggerType() != SubEventTriggerType.BLOCK_INTERACTION) {
                continue;
            }
            
            try {
                List<String> triggerBlockTypes = trigger.getBlockTypes();
                if (triggerBlockTypes != null && triggerBlockTypes.contains(blockType)) {
                    executeSubEvent(subEvent, world);
                }
            } catch (Exception e) {
                System.err.println("[Event:"+eventId+"] Error processing block interaction trigger for sub-event: " + subEventId);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Execute a sub-event and all its actions.
     */
    private void executeSubEvent(SubEvent subEvent, World world) {
        if (subEvent == null) {
            return;
        }
        
        String subEventId = subEvent.getId();
        if (subEventId == null) {
            System.err.println("[Event:"+eventId+"] Cannot execute sub-event with null ID");
            return;
        }
        
        // Mark as executed (one-time per event instance)
        // This also checks for execution limit to prevent infinite loops
        if (!subEventState.markSubEventExecuted(subEventId)) {
            // Already executed or limit reached
            return;
        }
        
        List<SubEventAction> actions = subEvent.getActions();
        if (actions == null || actions.isEmpty()) {
            System.out.println("[Event:"+eventId+"] Sub-event '" + subEventId + "' triggered but has no actions");
            return;
        }
        
        System.out.println("[Event:"+eventId+"] Executing sub-event: " + subEventId + " (total executed: " + subEventState.getExecutedCount() + ")");
        
        // Execute all actions for this sub-event
        for (SubEventAction action : actions) {
            if (action == null) {
                System.err.println("[Event:"+eventId+"] Null action in sub-event '" + subEventId + "', skipping");
                continue;
            }
            
            try {
                executeSubEventAction(action, world);
            } catch (Exception e) {
                System.err.println("[Event:"+eventId+"] Error executing sub-event action for '" + subEventId + "'");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Execute a single sub-event action.
     * Action execution logic will be added in DefaultEvent or overridden in subclasses.
     */
    protected void executeSubEventAction(SubEventAction action, World world) {
        // Default implementation does nothing
        // Override in subclasses or DefaultEvent to implement specific action types
        System.out.println("[Event:"+eventId+"] Action type not implemented: " + action.getActionType());
    }
    
    // ==================== Getters ====================
    
    public String getEventId() {
        return eventId;
    }
    
    public EventConfig getConfig() {
        return config;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public long getStartTimeMs() {
        return startTimeMs;
    }
    
    public long getElapsedTimeMs() {
        if (!isRunning) {
            return 0;
        }
        return System.currentTimeMillis() - startTimeMs;
    }
}
