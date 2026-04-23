package me.voidyll.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import me.voidyll.commands.TriggerSpawnCommand;
import me.voidyll.data.ActiveSpawnGroupManager;
import me.voidyll.data.RoleConfigManager;
import me.voidyll.data.RoleDefinition;
import me.voidyll.utils.WorldUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Spawn Director System manages automated spawning of horde and special enemies.
 * 
 * Features:
 * - Horde spawning: 3-wave spawns triggered by randomized timer (2-3 min default)
 * - Special spawning: Single spawns on fixed timer (60s default)
 * - Entity cap enforcement (200 total, 4 specials by default)
 * - Timer controls via commands and API
 */
public class SpawnDirectorSystem {
    
    private static final int DEFAULT_ENTITY_CAP = 200;
    private static final int DEFAULT_SPECIAL_CAP = 4;
    private static final int DEFAULT_HORDE_TIMER_MIN_MS = 120000; // 2 minutes
    private static final int DEFAULT_HORDE_TIMER_MAX_MS = 180000; // 3 minutes
    private static final int DEFAULT_SPECIAL_TIMER_MS = 60000; // 60 seconds
    private static final int HORDE_WAVE_INTERVAL_MS = 30000; // 30 seconds between waves
    private static final int HORDE_WAVE_COUNT = 3;
    
    private final TriggerSpawnCommand spawnCommand;
    private final ActiveSpawnGroupManager groupManager;
    private final RoleConfigManager roleConfigManager;
    
    // Horde timer state
    private int hordeTimerMinMs = DEFAULT_HORDE_TIMER_MIN_MS;
    private int hordeTimerMaxMs = DEFAULT_HORDE_TIMER_MAX_MS;
    private long hordeTimerRemainingMs;
    private boolean hordeTimerPaused = false;
    private boolean hordeWaveInProgress = false;
    private int hordeWavesRemaining = 0;
    private long hordeWaveIntervalRemainingMs = 0;
    
    // Special timer state
    private int specialTimerMs = DEFAULT_SPECIAL_TIMER_MS;
    private long specialTimerRemainingMs;
    private boolean specialTimerPaused = false;
    
    // Entity caps
    private int entityCap = DEFAULT_ENTITY_CAP;
    private int specialCap = DEFAULT_SPECIAL_CAP;
    
    // Tracking
    private long lastTickTime = System.currentTimeMillis();
    private long lastDebugTimeMs = 0;
    private final Random random = new Random();
    
    public SpawnDirectorSystem(TriggerSpawnCommand spawnCommand, ActiveSpawnGroupManager groupManager, RoleConfigManager roleConfigManager) {
        this.spawnCommand = spawnCommand;
        this.groupManager = groupManager;
        this.roleConfigManager = roleConfigManager;
        
        // Initialize timers
        this.hordeTimerRemainingMs = rollHordeTimer();
        this.specialTimerRemainingMs = specialTimerMs;
    }
    
    public void registerSystems(ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        entityStoreRegistry.registerSystem(new DirectorTickingSystem());
    }
    
    // ==================== Horde Timer Controls ====================
    
    public void pauseHordeTimer() {
        hordeTimerPaused = true;
    }
    
    public void unpauseHordeTimer() {
        hordeTimerPaused = false;
    }
    
    public void restartHordeTimer() {
        hordeTimerRemainingMs = rollHordeTimer();
        hordeWaveInProgress = false;
        hordeWavesRemaining = 0;
        hordeWaveIntervalRemainingMs = 0;
        lastTickTime = System.currentTimeMillis();
    }
    
    public void setHordeTimerRange(int minMs, int maxMs) {
        this.hordeTimerMinMs = minMs;
        this.hordeTimerMaxMs = maxMs;
    }
    
    public long getHordeTimerRemainingMs() {
        return hordeTimerRemainingMs;
    }
    
    public boolean isHordeTimerPaused() {
        return hordeTimerPaused;
    }
    
    // ==================== Special Timer Controls ====================
    
    public void pauseSpecialTimer() {
        specialTimerPaused = true;
    }
    
    public void unpauseSpecialTimer() {
        specialTimerPaused = false;
    }
    
    public void restartSpecialTimer() {
        specialTimerRemainingMs = specialTimerMs;
        lastTickTime = System.currentTimeMillis();
    }
    
    public void setSpecialTimerMs(int ms) {
        this.specialTimerMs = ms;
    }
    
    public long getSpecialTimerRemainingMs() {
        return specialTimerRemainingMs;
    }
    
    public boolean isSpecialTimerPaused() {
        return specialTimerPaused;
    }
    
    // ==================== Entity Cap Controls ====================
    
    public void setEntityCap(int cap) {
        this.entityCap = cap;
    }
    
    public int getEntityCap() {
        return entityCap;
    }
    
    public void setSpecialCap(int cap) {
        this.specialCap = cap;
    }
    
    public int getSpecialCap() {
        return specialCap;
    }
    
    // ==================== Event Integration ====================
    
    /**
     * Apply event values to the spawn director.
     * Restarts and unpauses both timers with event values.
     */
    public void applyEventValues(int eventHordeMin, int eventHordeMax, int eventHordeWaveInterval, 
                                  int eventHordeWaveCount, int eventSpecialTimer) {
        // Set event values
        this.hordeTimerMinMs = eventHordeMin;
        this.hordeTimerMaxMs = eventHordeMax;
        // Note: hordeWaveIntervalMs and hordeWaveCount are currently constants
        // If you want to make them configurable per event, add fields and use them
        this.specialTimerMs = eventSpecialTimer;
        
        // Restart and unpause timers
        restartHordeTimer();
        restartSpecialTimer();
        unpauseHordeTimer();
        unpauseSpecialTimer();
    }
    
    /**
     * Restore default values to the spawn director.
     * Restarts and unpauses both timers with default values.
     */
    public void restoreDefaultValues() {
        // Restore defaults
        this.hordeTimerMinMs = DEFAULT_HORDE_TIMER_MIN_MS;
        this.hordeTimerMaxMs = DEFAULT_HORDE_TIMER_MAX_MS;
        this.specialTimerMs = DEFAULT_SPECIAL_TIMER_MS;
        
        // Restart and unpause timers
        restartHordeTimer();
        restartSpecialTimer();
        unpauseHordeTimer();
        unpauseSpecialTimer();
    }
    
    // ==================== Internal Helpers ====================
    
    private long rollHordeTimer() {
        return hordeTimerMinMs + random.nextInt(hordeTimerMaxMs - hordeTimerMinMs + 1);
    }
    
    private Set<String> getSpecialRoleNames() {
        return roleConfigManager.getRolesForType("special").stream()
            .map(RoleDefinition::getEntityName)
            .collect(Collectors.toSet());
    }
    
    private int countSpecialEnemies(Store<EntityStore> store) {
        final int[] count = {0};
        Set<String> specialRoleNames = getSpecialRoleNames();
        Query<EntityStore> npcQuery = NPCEntity.getComponentType();
        
        store.forEachChunk(npcQuery, (ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> buffer) -> {
            for (int i = 0; i < chunk.size(); i++) {
                Ref<EntityStore> npcRef = chunk.getReferenceTo(i);
                NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());
                
                if (npcEntity != null && npcEntity.getRoleName() != null) {
                    String roleName = npcEntity.getRoleName();
                    if (specialRoleNames.contains(roleName)) {
                        count[0]++;
                    }
                }
            }
        });
        
        return count[0];
    }
    
    private void triggerHordeWave() {
        List<Integer> activeGroups = groupManager.getAllActiveGroups();
        if (activeGroups.isEmpty()) {
            // No active groups, spawn attempt does nothing
            return;
        }
        
        // Trigger horde spawn for all active groups
        spawnCommand.spawnForGroups(activeGroups, "horde", null);
    }
    
    private void triggerSpecialSpawn() {
        List<Integer> activeGroups = groupManager.getAllActiveGroups();
        if (activeGroups.isEmpty()) {
            // No active groups, spawn attempt does nothing
            return;
        }
        
        // Trigger special spawn for all active groups
        spawnCommand.spawnForGroups(activeGroups, "special", null);
    }

    private void sendDebugToAllPlayers(World world, String message) {
        if (world == null || !DebugMessageSettings.areDebugMessagesEnabled()) {
            return;
        }

        List<Player> players = world.getPlayers();
        for (Player player : players) {
            player.sendMessage(Message.raw(message));
        }
    }
    
    // ==================== Ticking System ====================
    
    private class DirectorTickingSystem extends EntityTickingSystem<EntityStore> {
        
        @Override
        public Query<EntityStore> getQuery() {
            // Query for players to ensure system ticks (needs at least one component type)
            return Player.getComponentType();
        }
        
        @Override
        public void tick(float delta, int tick, ArchetypeChunk<EntityStore> chunk, 
                         Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
            
            long currentTime = System.currentTimeMillis();
            long deltaMs = currentTime - lastTickTime;
            lastTickTime = currentTime;
            
            // Get current entity counts
            int totalNpcs = StuckNPCCleanupSystem.getTotalNpcCount();
            int specialNpcs = countSpecialEnemies(store);

            if (currentTime - lastDebugTimeMs >= 1000) {
                lastDebugTimeMs = currentTime;
                World world = WorldUtil.getGameWorld();
                long hordeSeconds = Math.max(0, hordeTimerRemainingMs) / 1000;
                long specialSeconds = Math.max(0, specialTimerRemainingMs) / 1000;
                String waveInfo = hordeWaveInProgress
                    ? " wavesRemaining=" + hordeWavesRemaining + " nextWaveIn=" + Math.max(0, hordeWaveIntervalRemainingMs) / 1000 + "s"
                    : "";
                sendDebugToAllPlayers(world,
                    "[SpawnDirector] horde=" + hordeSeconds + "s (paused=" + hordeTimerPaused + ")" +
                    " special=" + specialSeconds + "s (paused=" + specialTimerPaused + ")" +
                    " totalNpcs=" + totalNpcs + " specialNpcs=" + specialNpcs + waveInfo);
            }
            
            // ==================== Horde Timer Logic ====================
            
            if (!hordeTimerPaused) {
                if (hordeWaveInProgress) {
                    // Currently spawning waves
                    hordeWaveIntervalRemainingMs -= deltaMs;
                    
                    if (hordeWaveIntervalRemainingMs <= 0) {
                        // Time to spawn next wave
                        triggerHordeWave();
                        hordeWavesRemaining--;
                        
                        if (hordeWavesRemaining > 0) {
                            // Reset interval for next wave
                            hordeWaveIntervalRemainingMs = HORDE_WAVE_INTERVAL_MS;
                        } else {
                            // All waves complete
                            hordeWaveInProgress = false;
                            restartHordeTimer();
                        }
                    }
                } else {
                    // Countdown to next horde event
                    hordeTimerRemainingMs -= deltaMs;
                    
                    if (hordeTimerRemainingMs <= 0) {
                        // Timer expired - check entity cap
                        if (totalNpcs < entityCap) {
                            // Start spawning 3 waves
                            hordeWaveInProgress = true;
                            hordeWavesRemaining = HORDE_WAVE_COUNT;
                            hordeWaveIntervalRemainingMs = 0; // Spawn first wave immediately
                        }
                        // If over cap, timer stays at 0 and will check again next tick
                    }
                }
            }
            
            // ==================== Special Timer Logic ====================
            
            if (!specialTimerPaused) {
                specialTimerRemainingMs -= deltaMs;
                
                if (specialTimerRemainingMs <= 0) {
                    // Timer expired - check special cap
                    if (specialNpcs < specialCap) {
                        // Spawn special and restart timer
                        triggerSpecialSpawn();
                        restartSpecialTimer();
                    }
                    // If at/over cap, timer stays at 0 and will check again next tick
                }
            }
        }
    }
}
