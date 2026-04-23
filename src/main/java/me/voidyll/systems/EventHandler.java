package me.voidyll.systems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.voidyll.systems.events.DefaultEvent;
import me.voidyll.systems.events.Event;
import me.voidyll.systems.events.EventConfig;
import me.voidyll.systems.events.SubEvent;
import me.voidyll.data.DoorStateData;
import me.voidyll.utils.CommandExecutor;
import me.voidyll.utils.WorldUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import com.google.gson.JsonSyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Event Handler manages game events that modify spawn director behavior.
 * Events are loaded from JSON configuration and can be triggered manually or by game events.
 */
public class EventHandler {
    private static final long EVENTS_CONFIG_CHECK_INTERVAL_MS = 1000L;
    
    private final Path configFilePath;
    private final Path trackedChangesFilePath;
    private final Gson gson;
    private final SpawnDirectorSystem spawnDirector;
    
    private Map<String, Event> events = new HashMap<>();
    private Event currentEvent = null;
    private final Object eventLock = new Object();  // Lock for thread-safe event operations
    private Map<String, Boolean> eventStartedStatus = new HashMap<>();
    
    // Block triggering support (coordinate-based)
    private Map<String, String> coordKeyToEventId = new HashMap<>();  // Maps "x,y,z" -> event ID
    private Map<String, Set<String>> eventIdToCoordKeys = new HashMap<>();  // Maps event ID -> coord keys
    private Set<String> triggeredCoordKeys = new HashSet<>();  // Tracks which coordinates have been triggered (one-time per session)
    
    // Door state tracking for reset functionality
    private List<DoorStateData> trackedDoorStates = new ArrayList<>();
    
    // Block change tracking for reset functionality (coordinate key -> original block type)
    private Map<String, String> trackedBlockChanges = new HashMap<>();
    
    private long lastTickTime = System.currentTimeMillis();
    private volatile long eventsConfigLastModifiedMs = -1L;
    private volatile long lastEventsConfigCheckMs = 0L;
    
    public EventHandler(Path pluginDataDirectory, SpawnDirectorSystem spawnDirector) {
        this.configFilePath = pluginDataDirectory.resolve("events.json");
        this.trackedChangesFilePath = pluginDataDirectory.resolve("tracked_changes.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.spawnDirector = spawnDirector;
        
        loadOrCreateEvents();
        eventsConfigLastModifiedMs = getEventsConfigLastModifiedMs();
        loadTrackedChanges();
    }
    
    public void registerSystems(ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        entityStoreRegistry.registerSystem(new EventTickingSystem());
    }
    
    // ==================== Event Management ====================
    
    /**
     * Start an event by ID.
     * If another event is running, it will be stopped first.
     * 
     * @param eventId The ID of the event to start
     * @return true if event was started successfully
     */
    public boolean startEvent(String eventId) {
        refreshEventsFromDiskIfChanged();
        Event event = events.get(eventId);
        if (event == null) {
            return false;
        }
        
        World world = WorldUtil.getGameWorld();
        if (world == null) {
            return false;
        }
        
        synchronized (eventLock) {
            // Stop current event if running
            if (currentEvent != null) {
                stopCurrentEventInternal();
            }
            
            // Apply event values to spawn director
            EventConfig config = event.getConfig();
            spawnDirector.applyEventValues(
                config.getHordeTimerMinMs(),
                config.getHordeTimerMaxMs(),
                config.getHordeWaveIntervalMs(),
                config.getHordeWaveCount(),
                config.getSpecialTimerMs()
            );
            
            // Start the event
            event.onStart(world);
            currentEvent = event;
            eventStartedStatus.put(eventId, true);
        }
        
        // Notify players (outside lock to avoid potential deadlock)
        broadcastToAllPlayers("[Event] " + eventId + " has started!");
        
        return true;
    }
    
    /**
     * Stop the currently running event.
     */
    public void stopCurrentEvent() {
        synchronized (eventLock) {
            stopCurrentEventInternal();
        }
    }
    
    /**
     * Internal method to stop event - must be called within synchronized block.
     */
    private void stopCurrentEventInternal() {
        if (currentEvent == null) {
            return;
        }
        
        String eventId = currentEvent.getEventId();
        
        World world = WorldUtil.getGameWorld();
        if (world != null) {
            currentEvent.onEnd(world);
        }
        
        // Restore spawn director defaults
        spawnDirector.restoreDefaultValues();
        
        currentEvent = null;
        
        // Notify players (outside world null check)
        broadcastToAllPlayers("[Event] " + eventId + " has ended!");
    }
    
    /**
     * Get the currently running event, or null if none.
     */
    public Event getCurrentEvent() {
        synchronized (eventLock) {
            return currentEvent;
        }
    }
    
    /**
     * Get all available events.
     */
    public Map<String, Event> getAllEvents() {
        refreshEventsFromDiskIfChanged();
        return new HashMap<>(events);
    }

    public Path getConfigFilePath() {
        return configFilePath;
    }
    
    /**
     * Get all registered trigger coordinate keys.
     * @return Set of "x,y,z" strings
     */
    public Set<String> getRegisteredTriggerCoordKeys() {
        refreshEventsFromDiskIfChanged();
        return new HashSet<>(coordKeyToEventId.keySet());
    }

    /**
     * Check if a coordinate is registered as a trigger for any event.
     */
    public boolean isTriggerCoordinate(int x, int y, int z) {
        refreshEventsFromDiskIfChanged();
        return coordKeyToEventId.containsKey(x + "," + y + "," + z);
    }
    
    /**
     * Check if an event has been started at least once.
     */
    public boolean hasEventBeenStarted(String eventId) {
        refreshEventsFromDiskIfChanged();
        return eventStartedStatus.getOrDefault(eventId, false);
    }
    
    /**
     * Reset all triggered coordinates so they can trigger events again.
     * Called by /reset command.
     */
    public void resetTriggerBlockStates() {
        triggeredCoordKeys.clear();
    }
    
    /**
     * Track a door state change for reset functionality.
     * This should be called when a SET_BLOCK_STATE action is executed.
     * 
     * @param x X coordinate of the door
     * @param y Y coordinate of the door
     * @param z Z coordinate of the door
     * @param state The state being set (e.g., "OpenDoorIn")
     */
    public void trackDoorState(int x, int y, int z, String state) {
        String posKey = x + "," + y + "," + z;
        
        // Remove any existing entry for this position
        trackedDoorStates.removeIf(door -> door.getPositionKey().equals(posKey));
        
        // Add the new door state
        DoorStateData doorData = new DoorStateData(x, y, z, state);
        trackedDoorStates.add(doorData);
        System.out.println("[EventHandler] Tracking door state: " + doorData);
        saveTrackedChanges();
    }
    
    /**
     * Reset all tracked doors to their opposite states.
     * Called by /reset command.
     * For example, OpenDoorIn becomes CloseDoorIn.
     */
    public void resetDoorStates() {
        if (trackedDoorStates.isEmpty()) {
            System.out.println("[EventHandler] No door states to reset.");
            return;
        }
        
        int resetCount = 0;
        for (DoorStateData door : trackedDoorStates) {
            String oppositeState = door.getOppositeState();
            try {
                String command = "block setstate " + door.getX() + " " + door.getY() + " " + door.getZ() + " " + oppositeState;
                CommandExecutor.executeCommandSync(command);
                System.out.println("[EventHandler] Reset door at (" + door.getX() + "," + door.getY() + "," + door.getZ() + ") from " + door.getState() + " to " + oppositeState);
                resetCount++;
            } catch (Exception e) {
                System.err.println("[EventHandler] Failed to reset door at (" + door.getX() + "," + door.getY() + "," + door.getZ() + "): " + e.getMessage());
            }
        }
        
        System.out.println("[EventHandler] Reset " + resetCount + " door(s) to their opposite states.");
    }
    
    /**
     * Clear all tracked door states.
     * Called by /reset command after resetting doors.
     */
    public void clearTrackedDoorStates() {
        trackedDoorStates.clear();
        saveTrackedChanges();
    }
    
    /**
     * Track a block change for reset functionality.
     * Stores the original block type at a coordinate before it is modified.
     * Only records the first change per coordinate (preserves the true original state).
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param originalBlockType The block type ID that was at this position before the change
     */
    public void trackBlockChange(int x, int y, int z, String originalBlockType) {
        String posKey = x + "," + y + "," + z;
        // Only track if we haven't already recorded this coordinate (preserve original state)
        if (!trackedBlockChanges.containsKey(posKey)) {
            trackedBlockChanges.put(posKey, originalBlockType);
            System.out.println("[EventHandler] Tracking block change at (" + posKey + ") original type: " + originalBlockType);
            saveTrackedChanges();
        }
    }
    
    /**
     * Reset all tracked block changes to their original states.
     * Called by /reset command.
     */
    public void resetBlockChanges() {
        if (trackedBlockChanges.isEmpty()) {
            System.out.println("[EventHandler] No block changes to reset.");
            return;
        }
        
        int resetCount = 0;
        for (Map.Entry<String, String> entry : trackedBlockChanges.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            String originalType = entry.getValue();
            
            try {
                String command = "block set " + x + " " + y + " " + z + " " + originalType;
                CommandExecutor.executeCommandSync(command);
                System.out.println("[EventHandler] Restored block at (" + entry.getKey() + ") to: " + originalType);
                resetCount++;
            } catch (Exception e) {
                System.err.println("[EventHandler] Failed to restore block at (" + entry.getKey() + "): " + e.getMessage());
            }
        }
        
        System.out.println("[EventHandler] Restored " + resetCount + " block(s) to original states.");
    }
    
    /**
     * Clear all tracked block changes.
     * Called by /reset command after restoring blocks.
     */
    public void clearTrackedBlockChanges() {
        trackedBlockChanges.clear();
        saveTrackedChanges();
    }
    
    // ==================== Tracked Changes Persistence ====================
    
    private void saveTrackedChanges() {
        try {
            File file = trackedChangesFilePath.toFile();
            file.getParentFile().mkdirs();
            
            Map<String, Object> data = new HashMap<>();
            data.put("blockChanges", trackedBlockChanges);
            data.put("doorStates", trackedDoorStates);
            
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[EventHandler] Error saving tracked_changes.json: " + e.getMessage());
        }
    }
    
    private void loadTrackedChanges() {
        File file = trackedChangesFilePath.toFile();
        if (!file.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            if (data == null) {
                return;
            }
            
            if (data.has("blockChanges") && !data.get("blockChanges").isJsonNull()) {
                Type blockChangesType = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> loadedBlockChanges = gson.fromJson(data.get("blockChanges"), blockChangesType);
                if (loadedBlockChanges != null) {
                    trackedBlockChanges = loadedBlockChanges;
                    System.out.println("[EventHandler] Loaded " + trackedBlockChanges.size() + " tracked block change(s) from disk.");
                }
            }
            
            if (data.has("doorStates") && !data.get("doorStates").isJsonNull()) {
                Type doorStatesType = new TypeToken<List<DoorStateData>>() {}.getType();
                List<DoorStateData> loadedDoorStates = gson.fromJson(data.get("doorStates"), doorStatesType);
                if (loadedDoorStates != null) {
                    trackedDoorStates = loadedDoorStates;
                    System.out.println("[EventHandler] Loaded " + trackedDoorStates.size() + " tracked door state(s) from disk.");
                }
            }
        } catch (IOException e) {
            System.err.println("[EventHandler] Error loading tracked_changes.json: " + e.getMessage());
        }
    }
    
    // ==================== External Notifications ====================
    
    /**
     * Called when a block is interacted with at a specific coordinate.
     * Can start events based on trigger coordinates, or notify the current event.
     */
    public void handleBlockInteraction(int x, int y, int z, String blockType) {
        refreshEventsFromDiskIfChanged();
        String coordKey = x + "," + y + "," + z;
        
        // Check if this coordinate triggers an event (one-time per session)
        if (!triggeredCoordKeys.contains(coordKey) && coordKeyToEventId.containsKey(coordKey)) {
            String eventId = coordKeyToEventId.get(coordKey);
            Set<String> associatedCoordKeys = eventIdToCoordKeys.get(eventId);
            if (associatedCoordKeys != null && !associatedCoordKeys.isEmpty()) {
                triggeredCoordKeys.addAll(associatedCoordKeys);
            } else {
                triggeredCoordKeys.add(coordKey);
            }
            startEvent(eventId);
            return;
        }
        
        // Notify current event if running (thread-safe access)
        Event event;
        synchronized (eventLock) {
            event = currentEvent;
        }
        if (event != null && blockType != null) {
            event.notifyBlockInteraction(blockType);
        }
    }
    
    /**
     * Called when a trigger zone is activated.
     * Can be used to trigger events or notify the current event.
     */
    public void handleTriggerZoneActivated(String zoneName) {
        Event event;
        synchronized (eventLock) {
            event = currentEvent;
        }
        if (event != null) {
            event.notifyTriggerZoneActivated(zoneName);
        }
        
        // Future: Add logic to start events based on trigger zones
    }
    
    /**
     * Called when a block is broken.
     * Notifies the current event if running.
     */
    public void handleBlockBroken(String blockType) {
        Event event;
        synchronized (eventLock) {
            event = currentEvent;
        }
        if (event != null) {
            event.notifyBlockBroken(blockType);
        }
    }
    
    /**
     * Called when an entity is killed.
     * Notifies the current event if running.
     */
    public void handleEntityKilled(String entityRole) {
        Event event;
        synchronized (eventLock) {
            event = currentEvent;
        }
        if (event != null) {
            event.notifyEntityKilled(entityRole);
        }
    }
    
    // ==================== Configuration ====================
    
    private void loadOrCreateEvents() {
        File file = configFilePath.toFile();
        if (!file.exists()) {
            createDefaultEventsConfig();
        } else {
            loadEventsConfig();
        }
        eventsConfigLastModifiedMs = getEventsConfigLastModifiedMs();
    }
    
    private void createDefaultEventsConfig() {
        List<EventConfig> defaultEvents = new ArrayList<>();
        saveEventsConfig(defaultEvents);
        loadEventsFromConfigs(defaultEvents);
    }
    
    private void loadEventsConfig() {
        try (FileReader reader = new FileReader(configFilePath.toFile())) {
            Type listType = new TypeToken<List<EventConfig>>(){}.getType();
            List<EventConfig> configs = gson.fromJson(reader, listType);
            
            if (configs == null) {
                configs = new ArrayList<>();
            }
            
            loadEventsFromConfigs(configs);
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Error loading events.json: " + e.getMessage());
            if (events.isEmpty()) {
                createDefaultEventsConfig();
            }
        }
    }
    
    private void loadEventsFromConfigs(List<EventConfig> configs) {
        events.clear();
        eventStartedStatus.clear();
        coordKeyToEventId.clear();
        eventIdToCoordKeys.clear();
        triggeredCoordKeys.clear();
        
        for (EventConfig config : configs) {
            if (config == null || config.getEventId() == null || config.getEventId().isEmpty()) {
                continue;
            }
            config.parseEndConditionType();
            
            // Create event instance (use DefaultEvent for now, can be extended later)
            Event event = new DefaultEvent(config.getEventId(), config);
            event.setEventHandler(this); // Pass EventHandler reference for tracking
            events.put(config.getEventId(), event);
            eventStartedStatus.put(config.getEventId(), false);
            
            // Register coordinate triggers if specified
            List<int[]> triggerCoords = config.getTriggerBlockCoordinates();
            if (triggerCoords == null) {
                continue;
            }
            for (int[] coord : triggerCoords) {
                if (coord == null || coord.length < 3) {
                    continue;
                }
                String coordKey = coord[0] + "," + coord[1] + "," + coord[2];
                eventIdToCoordKeys
                    .computeIfAbsent(config.getEventId(), key -> new HashSet<>())
                    .add(coordKey);
                // Check for duplicate coordinate assignments
                if (coordKeyToEventId.containsKey(coordKey)) {
                    System.err.println("[EventHandler] Warning: Coordinate '" + coordKey +
                        "' assigned to multiple events. Existing: " + coordKeyToEventId.get(coordKey) +
                        ", New: " + config.getEventId() + ". Using new assignment.");
                }
                coordKeyToEventId.put(coordKey, config.getEventId());
            }
        }
    }
    
    private void saveEventsConfig(List<EventConfig> configs) {
        try {
            File file = configFilePath.toFile();
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(configs, writer);
            }
            eventsConfigLastModifiedMs = file.lastModified();
        } catch (IOException e) {
            System.err.println("Error saving events.json: " + e.getMessage());
        }
    }
    
    /**
     * Add a new event from config and save to disk.
     * @param config The event configuration to add
     * @return true if added successfully, false if event ID already exists
     */
    public boolean addEvent(EventConfig config) {
        refreshEventsFromDiskIfChanged();
        if (events.containsKey(config.getEventId())) {
            return false;
        }
        
        // Load current configs from disk, add new one, save
        List<EventConfig> configs = loadEventsConfigList();
        configs.add(config);
        saveEventsConfig(configs);
        
        // Reload all events to rebuild internal state
        loadEventsFromConfigs(configs);
        return true;
    }
    
    /**
     * Remove an event by ID and save to disk.
     * @param eventId The event ID to remove
     * @return true if removed, false if not found
     */
    public boolean removeEvent(String eventId) {
        refreshEventsFromDiskIfChanged();
        if (!events.containsKey(eventId)) {
            return false;
        }
        
        // Stop event if it's running
        synchronized (eventLock) {
            if (currentEvent != null && currentEvent.getEventId().equals(eventId)) {
                stopCurrentEventInternal();
            }
        }
        
        List<EventConfig> configs = loadEventsConfigList();
        configs.removeIf(c -> eventId.equals(c.getEventId()));
        saveEventsConfig(configs);
        loadEventsFromConfigs(configs);
        return true;
    }
    
    /**
     * Load the current event configs as a mutable list (for add/remove operations).
     */
    private List<EventConfig> loadEventsConfigList() {
        refreshEventsFromDiskIfChanged();
        File file = configFilePath.toFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<EventConfig>>(){}.getType();
            List<EventConfig> configs = gson.fromJson(reader, listType);
            return configs != null ? new ArrayList<>(configs) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading events.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Reload events from disk.
     */
    public void reloadEvents() {
        loadEventsConfig();
        eventsConfigLastModifiedMs = getEventsConfigLastModifiedMs();
    }
    
    /**
     * Add a sub-event to an existing event and save to disk.
     * @param eventId The event ID to add the sub-event to
     * @param subEvent The sub-event to add
     * @return true if added successfully, false if event not found
     */
    public boolean addSubEvent(String eventId, SubEvent subEvent) {
        refreshEventsFromDiskIfChanged();
        if (!events.containsKey(eventId)) {
            return false;
        }
        
        List<EventConfig> configs = loadEventsConfigList();
        for (EventConfig config : configs) {
            if (eventId.equals(config.getEventId())) {
                List<SubEvent> subEvents = config.getSubEvents();
                subEvents.add(subEvent);
                config.setSubEvents(subEvents);
                break;
            }
        }
        saveEventsConfig(configs);
        loadEventsFromConfigs(configs);
        return true;
    }
    
    /**
     * Remove a sub-event from an existing event and save to disk.
     * @param eventId The event ID containing the sub-event
     * @param subEventId The sub-event ID to remove
     * @return true if removed, false if event or sub-event not found
     */
    public boolean removeSubEvent(String eventId, String subEventId) {
        refreshEventsFromDiskIfChanged();
        if (!events.containsKey(eventId)) {
            return false;
        }
        
        List<EventConfig> configs = loadEventsConfigList();
        boolean removed = false;
        for (EventConfig config : configs) {
            if (eventId.equals(config.getEventId())) {
                List<SubEvent> subEvents = config.getSubEvents();
                removed = subEvents.removeIf(se -> subEventId.equals(se.getId()));
                config.setSubEvents(subEvents);
                break;
            }
        }
        
        if (removed) {
            saveEventsConfig(configs);
            loadEventsFromConfigs(configs);
        }
        return removed;
    }
    
    // ==================== Utilities ====================
    
    private void broadcastToAllPlayers(String message) {
        World world = WorldUtil.getGameWorld();
        if (world == null) {
            return;
        }
        
        List<Player> players = world.getPlayers();
        for (Player player : players) {
            player.sendMessage(Message.raw(message));
        }
    }

    private long getEventsConfigLastModifiedMs() {
        File file = configFilePath.toFile();
        if (!file.exists()) {
            return -1L;
        }
        return file.lastModified();
    }

    private void refreshEventsFromDiskIfChanged() {
        long now = System.currentTimeMillis();
        if (now - lastEventsConfigCheckMs < EVENTS_CONFIG_CHECK_INTERVAL_MS) {
            return;
        }
        lastEventsConfigCheckMs = now;

        long diskLastModified = getEventsConfigLastModifiedMs();
        if (diskLastModified == -1L) {
            return;
        }

        if (eventsConfigLastModifiedMs == -1L) {
            eventsConfigLastModifiedMs = diskLastModified;
            return;
        }

        if (diskLastModified != eventsConfigLastModifiedMs) {
            loadEventsConfig();
            eventsConfigLastModifiedMs = getEventsConfigLastModifiedMs();
        }
    }
    
    // ==================== Ticking System ====================
    
    private class EventTickingSystem extends EntityTickingSystem<EntityStore> {
        
        @Override
        public Query<EntityStore> getQuery() {
            // Query for players to ensure system ticks
            return Player.getComponentType();
        }
        
        @Override
        public void tick(float delta, int tick, ArchetypeChunk<EntityStore> chunk, 
                         Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
            refreshEventsFromDiskIfChanged();
            
            Event event;
            synchronized (eventLock) {
                event = currentEvent;
            }
            
            if (event == null) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            long deltaMs = currentTime - lastTickTime;
            lastTickTime = currentTime;
            
            World world = WorldUtil.getGameWorld();
            if (world == null) {
                return;
            }
            
            // Tick the current event
            event.onTick(deltaMs, world);
            
            // Check if event should end
            if (event.checkEndCondition()) {
                stopCurrentEvent();
            }
        }
    }
}
