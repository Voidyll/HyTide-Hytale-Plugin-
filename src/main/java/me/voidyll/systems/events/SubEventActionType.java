package me.voidyll.systems.events;

/**
 * Enum for different types of sub-event actions.
 */
public enum SubEventActionType {
    /**
     * Add blocks at specific coordinates.
     */
    ADD_BLOCKS,
    
    /**
     * Remove blocks at specific coordinates.
     */
    REMOVE_BLOCKS,
    
    /**
     * Spawn NPCs of a specific type at a location.
     */
    SPAWN_NPC,
    
    /**
     * Unlock a block type so it can be interacted with.
     */
    UNLOCK_INTERACTION,
    
    /**
     * Despawn all enemy NPCs.
     */
    DESPAWN_ALL_NPCS,
    
    /**
     * Execute a custom command.
     */
    EXECUTE_COMMAND,
    
    /**
     * Set the state of a block at specific coordinates (e.g., door states).
     */
    SET_BLOCK_STATE
}
