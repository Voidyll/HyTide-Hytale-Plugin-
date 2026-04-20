package me.voidyll.systems.events;

/**
 * Enum for different types of sub-event triggers.
 */
public enum SubEventTriggerType {
    /**
     * Triggered after a certain amount of time has passed since the event started.
     */
    TIMER,
    
    /**
     * Triggered when any of a set of block types are interacted with.
     */
    BLOCK_INTERACTION,
    
    /**
     * Triggered when a certain number of entities have been killed since the event started.
     */
    ENTITY_KILLED,
    
    /**
     * Triggered when another sub-event completes.
     */
    SUB_EVENT_COMPLETION
}
