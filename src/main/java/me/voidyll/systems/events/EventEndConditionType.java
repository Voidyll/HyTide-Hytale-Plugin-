package me.voidyll.systems.events;

/**
 * Types of end conditions for events.
 */
public enum EventEndConditionType {
    /** Event ends after a specific duration */
    TIMER,
    
    /** Event ends when a specific trigger zone is activated */
    TRIGGER_ZONE,
    
    /** Event ends when X blocks of a specific type are broken */
    BLOCK_BREAK,
    
    /** Event ends when a specific entity type is killed */
    ENTITY_KILLED,
    
    /** Event ends when a specific block type is interacted with */
    BLOCK_INTERACTION
}
