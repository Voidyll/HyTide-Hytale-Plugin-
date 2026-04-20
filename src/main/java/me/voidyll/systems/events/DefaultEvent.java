package me.voidyll.systems.events;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import me.voidyll.utils.CommandExecutor;

/**
 * Default event implementation with no custom logic.
 * Can be extended for events with custom behavior.
 */
public class DefaultEvent extends Event {
    
    public DefaultEvent(String eventId, EventConfig config) {
        super(eventId, config);
    }
    
    @Override
    public void onStart(World world) {
        super.onStart(world);
        // Add custom initialization if needed
    }
    
    @Override
    public void onTick(long deltaMs, World world) {
        super.onTick(deltaMs, world);
        // Add custom per-tick logic if needed
    }
    
    @Override
    public void onEnd(World world) {
        super.onEnd(world);
        // Add custom cleanup if needed
    }
    
    @Override
    protected void executeSubEventAction(SubEventAction action, World world) {
        if (action == null) {
            return;
        }
        
        SubEventActionType actionType = action.getActionType();
        JsonObject actionData = action.getActionData();
        
        switch (actionType) {
            case EXECUTE_COMMAND:
                handleExecuteCommand(actionData);
                break;
                
            case REMOVE_BLOCKS:
                handleRemoveBlocks(actionData, world);
                break;
                
            case ADD_BLOCKS:
                handleAddBlocks(actionData, world);
                break;
                
            case DESPAWN_ALL_NPCS:
                handleDespawnAllNpcs();
                break;
                
            case SPAWN_NPC:
                handleSpawnNpc(actionData, world);
                break;
                
            case UNLOCK_INTERACTION:
                handleUnlockInteraction(actionData);
                break;
                
            case SET_BLOCK_STATE:
                handleSetBlockState(actionData);
                break;
                
            default:
                System.out.println("[Event:"+eventId+"] Unknown action type: " + actionType);
        }
    }
    
    /**
     * Execute a custom command.
     * Expected format: { "command": "command string" }
     */
    private void handleExecuteCommand(JsonObject data) {
        if (data == null || !data.has("command")) {
            System.err.println("[Event:"+eventId+"] EXECUTE_COMMAND action missing 'command' field");
            return;
        }
        
        String command = data.get("command").getAsString();
        if (command == null || command.isEmpty()) {
            System.err.println("[Event:"+eventId+"] EXECUTE_COMMAND has null or empty command");
            return;
        }
        
        // Execute command asynchronously to avoid blocking game thread
        CommandExecutor.executeCommand(command).exceptionally(ex -> {
            System.err.println("[Event:"+eventId+"] Failed to execute command '" + command + "'");
            ex.printStackTrace();
            return null;
        });
        System.out.println("[Event:"+eventId+"] Executing command async: " + command);
    }
    
    /**
     * Remove blocks at specific coordinates.
     * Expected format: { "coordinates": [[x1,y1,z1], [x2,y2,z2], ...] }
     */
    private void handleRemoveBlocks(JsonObject data, World world) {
        if (data == null || !data.has("coordinates")) {
            System.err.println("[Event:"+eventId+"] REMOVE_BLOCKS action missing 'coordinates' field");
            return;
        }
        
        JsonArray coordsArray = data.getAsJsonArray("coordinates");
        if (coordsArray == null) {
            System.err.println("[Event:"+eventId+"] REMOVE_BLOCKS coordinates is null");
            return;
        }
        
        for (int i = 0; i < coordsArray.size(); i++) {
            try {
                JsonArray coord = coordsArray.get(i).getAsJsonArray();
                if (coord == null || coord.size() < 3) {
                    System.err.println("[Event:"+eventId+"] Invalid coordinate format at index " + i);
                    continue;
                }
                
                int x = coord.get(0).getAsInt();
                int y = coord.get(1).getAsInt();
                int z = coord.get(2).getAsInt();
                
                // Read and track the original block type before removing
                trackOriginalBlockType(world, x, y, z);
                
                // Use command to remove block at coordinate (async)
                String command = "block set " + x + " " + y + " " + z + " empty";
                final int index = i;
                CommandExecutor.executeCommand(command).exceptionally(ex -> {
                    System.err.println("[Event:"+eventId+"] Error removing block at index " + index);
                    ex.printStackTrace();
                    return null;
                });
            } catch (Exception e) {
                System.err.println("[Event:"+eventId+"] Error processing block removal at index " + i);
                e.printStackTrace();
            }
        }
        
        System.out.println("[Event:"+eventId+"] Initiated removal of " + coordsArray.size() + " blocks");
    }
    
    /**
     * Add blocks at specific coordinates.
     * Expected format: { "coordinates": [[x1,y1,z1], [x2,y2,z2], ...], "blockType": "block_type" }
     */
    private void handleAddBlocks(JsonObject data, World world) {
        if (data == null || !data.has("coordinates") || !data.has("blockType")) {
            System.err.println("[Event:"+eventId+"] ADD_BLOCKS action missing 'coordinates' or 'blockType' field");
            return;
        }
        
        String blockType = data.get("blockType").getAsString();
        if (blockType == null || blockType.isEmpty()) {
            System.err.println("[Event:"+eventId+"] ADD_BLOCKS has null or empty blockType");
            return;
        }
        
        JsonArray coordsArray = data.getAsJsonArray("coordinates");
        if (coordsArray == null) {
            System.err.println("[Event:"+eventId+"] ADD_BLOCKS coordinates is null");
            return;
        }
        
        for (int i = 0; i < coordsArray.size(); i++) {
            try {
                JsonArray coord = coordsArray.get(i).getAsJsonArray();
                if (coord == null || coord.size() < 3) {
                    System.err.println("[Event:"+eventId+"] Invalid coordinate format at index " + i);
                    continue;
                }
                
                int x = coord.get(0).getAsInt();
                int y = coord.get(1).getAsInt();
                int z = coord.get(2).getAsInt();
                
                // Read and track the original block type before adding
                trackOriginalBlockType(world, x, y, z);
                
                // Use command to place block at coordinate (async)
                String command = "block set " + x + " " + y + " " + z + " " + blockType;
                final int index = i;
                CommandExecutor.executeCommand(command).exceptionally(ex -> {
                    System.err.println("[Event:"+eventId+"] Error adding block at index " + index);
                    ex.printStackTrace();
                    return null;
                });
            } catch (Exception e) {
                System.err.println("[Event:"+eventId+"] Error processing block addition at index " + i);
                e.printStackTrace();
            }
        }
        
        System.out.println("[Event:"+eventId+"] Initiated addition of " + coordsArray.size() + " blocks of type '" + blockType + "'");
    }
    
    /**
     * Read the current block type at a coordinate and track it in EventHandler for reset.
     */
    private void trackOriginalBlockType(World world, int x, int y, int z) {
        try {
            long chunkKey = ChunkUtil.indexChunkFromBlock(x, z);
            BlockAccessor chunk = world.getChunkIfLoaded(chunkKey);
            if (chunk != null) {
                BlockType originalType = chunk.getBlockType(x, y, z);
                String originalTypeId = (originalType != null) ? originalType.getId() : "air";
                
                if (eventHandler != null) {
                    eventHandler.getClass()
                        .getMethod("trackBlockChange", int.class, int.class, int.class, String.class)
                        .invoke(eventHandler, x, y, z, originalTypeId);
                }
            } else {
                System.err.println("[Event:"+eventId+"] Chunk not loaded for block at (" + x + "," + y + "," + z + "), tracking as air");
                if (eventHandler != null) {
                    eventHandler.getClass()
                        .getMethod("trackBlockChange", int.class, int.class, int.class, String.class)
                        .invoke(eventHandler, x, y, z, "air");
                }
            }
        } catch (Exception e) {
            System.err.println("[Event:"+eventId+"] Failed to track original block type at (" + x + "," + y + "," + z + ")");
            e.printStackTrace();
        }
    }
    
    /**
     * Despawn all NPCs.
     * Expected format: { } (no parameters needed)
     */
    private void handleDespawnAllNpcs() {
        // Execute command asynchronously
        CommandExecutor.executeCommand("npc clean --confirm").exceptionally(ex -> {
            System.err.println("[Event:"+eventId+"] Error despawning NPCs");
            ex.printStackTrace();
            return null;
        });
        System.out.println("[Event:"+eventId+"] Initiating NPC despawn");
    }
    
    /**
     * Spawn an NPC of a specific type.
     * Expected format: { "npcType": "HyTide_Shadow_Knight", "x": 0, "y": 0, "z": 0 }
     */
    private void handleSpawnNpc(JsonObject data, World world) {
        if (data == null || !data.has("npcType") || !data.has("x") || !data.has("y") || !data.has("z")) {
            System.err.println("[Event:"+eventId+"] SPAWN_NPC action missing required fields (npcType, x, y, z)");
            return;
        }
        
        String npcType = data.get("npcType").getAsString();
        if (npcType == null || npcType.isEmpty()) {
            System.err.println("[Event:"+eventId+"] SPAWN_NPC has null or empty npcType");
            return;
        }
        
        int x = data.get("x").getAsInt();
        int y = data.get("y").getAsInt();
        int z = data.get("z").getAsInt();
        
        // Execute command asynchronously
        String command = "spawn " + npcType + " " + x + " " + y + " " + z;
        CommandExecutor.executeCommand(command).exceptionally(ex -> {
            System.err.println("[Event:"+eventId+"] Error spawning NPC '" + npcType + "'");
            ex.printStackTrace();
            return null;
        });
        System.out.println("[Event:"+eventId+"] Spawning NPC: " + npcType + " at (" + x + "," + y + "," + z + ")");
    }
    
    /**
     * Unlock an interaction for a block type.
     * Expected format: { "blockType": "lever_wall" }
     * (Extended implementation would require integration with the trigger system)
     */
    private void handleUnlockInteraction(JsonObject data) {
        if (data == null || !data.has("blockType")) {
            System.err.println("[Event:"+eventId+"] UNLOCK_INTERACTION action missing 'blockType' field");
            return;
        }
        
        String blockType = data.get("blockType").getAsString();
        System.out.println("[Event:"+eventId+"] Unlocked interaction for block type: " + blockType);
        
        // This would require integration with the trigger system to actually unlock interactions
        // For now, just log it
    }
    
    /**
     * Set the state of a block at specific coordinates.
     * Expected format: { "x": 100, "y": 64, "z": 200, "state": "OpenDoorIn" }
     */
    private void handleSetBlockState(JsonObject data) {
        if (data == null || !data.has("x") || !data.has("y") || !data.has("z") || !data.has("state")) {
            System.err.println("[Event:"+eventId+"] SET_BLOCK_STATE action missing required fields (x, y, z, state)");
            return;
        }
        
        int x = data.get("x").getAsInt();
        int y = data.get("y").getAsInt();
        int z = data.get("z").getAsInt();
        String state = data.get("state").getAsString();
        
        if (state == null || state.isEmpty()) {
            System.err.println("[Event:"+eventId+"] SET_BLOCK_STATE has null or empty state");
            return;
        }
        
        // Execute command asynchronously
        String command = "block setstate " + x + " " + y + " " + z + " " + state;
        CommandExecutor.executeCommand(command).whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("[Event:"+eventId+"] Error setting block state");
                ex.printStackTrace();
            } else {
                System.out.println("[Event:"+eventId+"] Set block state at (" + x + "," + y + "," + z + ") to: " + state);
                
                // Track this door state change in EventHandler for reset functionality
                if (eventHandler != null) {
                    try {
                        // Use reflection to call trackDoorState method
                        eventHandler.getClass()
                            .getMethod("trackDoorState", int.class, int.class, int.class, String.class)
                            .invoke(eventHandler, x, y, z, state);
                    } catch (Exception e) {
                        System.err.println("[Event:"+eventId+"] Failed to track door state");
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
