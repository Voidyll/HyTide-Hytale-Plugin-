package me.voidyll.systems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import me.voidyll.utils.WorldUtil;


/**
 * System that tracks NPC positions and removes NPCs that are stuck (haven't moved
 * more than 0.25 blocks in 30 seconds while having an active target and not near players).
 */
public class StuckNPCCleanupSystem {
    
    private static final float STUCK_DISTANCE_THRESHOLD = 0.25f;
    private static final long STUCK_TIME_THRESHOLD_MS = 30000; // 30 seconds
    private static final double PLAYER_PROXIMITY_THRESHOLD = 15.0; // 15 blocks


    /**
     * Tracks position history for an NPC.
     */
    private static class NPCPositionHistory {
        Vector3d position30SecondsAgo;
        long timestamp30SecondsAgo;
        Vector3d currentPosition;
        long currentTimestamp;
        UUID entityUuid;
        Ref<EntityStore> entityRef;

        NPCPositionHistory(Vector3d initialPosition, long timestamp, UUID uuid, Ref<EntityStore> ref) {
            this.position30SecondsAgo = new Vector3d(initialPosition);
            this.timestamp30SecondsAgo = timestamp;
            this.currentPosition = new Vector3d(initialPosition);
            this.currentTimestamp = timestamp;
            this.entityUuid = uuid;
            this.entityRef = ref;
        }

        void updatePosition(Vector3d newPosition, long timestamp) {
            currentPosition = new Vector3d(newPosition);
            currentTimestamp = timestamp;
        }

        void resetReferencePoint() {
            position30SecondsAgo = new Vector3d(currentPosition);
            timestamp30SecondsAgo = currentTimestamp;
        }

        boolean hasBeenTrackedFor30Seconds(long currentTime) {
            return (currentTime - timestamp30SecondsAgo) >= STUCK_TIME_THRESHOLD_MS;
        }

        double getDistanceMovedIn30Seconds() {
            if (position30SecondsAgo == null || currentPosition == null) {
                return Double.MAX_VALUE; // Assume moved if we don't have data
            }
            double dx = currentPosition.x - position30SecondsAgo.x;
            double dy = currentPosition.y - position30SecondsAgo.y;
            double dz = currentPosition.z - position30SecondsAgo.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    private final Map<UUID, NPCPositionHistory> trackedNPCs = new HashMap<>();
    private final Set<UUID> entitiesToRemove = new HashSet<>();
    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 1000; // Check every 1000ms (1 second)
    private static volatile int totalNpcCount = 0;

    public static int getTotalNpcCount() {
        return totalNpcCount;
    }

    public static void resetTotalNpcCount() {
        totalNpcCount = 0;
    }

    public void registerSystems(ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        entityStoreRegistry.registerSystem(new NPCTrackingSystem());
    }

    private class NPCTrackingSystem extends EntityTickingSystem<EntityStore> {

        @Override
        public Query<EntityStore> getQuery() {
            // Query for all NPCs
            return NPCEntity.getComponentType();
        }

        @Override
        public void tick(float delta, int tick, ArchetypeChunk<EntityStore> chunk, 
                         Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
            
            long currentTime = System.currentTimeMillis();
            
            // Only perform checks every second
            if (currentTime - lastCheckTime < CHECK_INTERVAL_MS) {
                return;
            }
            lastCheckTime = currentTime;

            World world = WorldUtil.getGameWorld();
            
            // Get Store to query all NPCs across all chunks
            EntityStore entityStore = world != null ? world.getEntityStore() : null;
            if (entityStore == null) {
                return;
            }
            
            Store<EntityStore> worldStore = entityStore.getStore();
            
            // Count total NPCs across all chunks
            final int[] totalNpcCount = {0};
            final Set<UUID> currentNpcUuids = new HashSet<>();
            Query<EntityStore> npcQuery = NPCEntity.getComponentType();
            worldStore.forEachChunk(npcQuery, (ArchetypeChunk<EntityStore> chunk_inner, CommandBuffer<EntityStore> buffer) -> {
                totalNpcCount[0] += chunk_inner.size();
                for (int i = 0; i < chunk_inner.size(); i++) {
                    Ref<EntityStore> npcRef = chunk_inner.getReferenceTo(i);
                    NPCEntity npcEntity = worldStore.getComponent(npcRef, NPCEntity.getComponentType());
                    if (npcEntity != null && npcEntity.getUuid() != null) {
                        currentNpcUuids.add(npcEntity.getUuid());
                    }
                }
            });

            StuckNPCCleanupSystem.totalNpcCount = totalNpcCount[0];
            
            // Clean up tracking list - remove NPCs that are no longer in the world
            if (trackedNPCs.size() > totalNpcCount[0]) {
                trackedNPCs.keySet().removeIf(uuid -> !currentNpcUuids.contains(uuid));
            }
            
            if (world != null) {
                sendDebugToAllPlayers(world, "[Stuck NPC Debug] System tick - Total NPCs in world: " + totalNpcCount[0] + ", Currently tracking: " + trackedNPCs.size());
            }

            // Get all player positions for proximity checks
            List<Vector3d> playerPositions = getPlayerPositions();

            // Process all NPCs in the store
            worldStore.forEachChunk(npcQuery, (ArchetypeChunk<EntityStore> chunk_inner, CommandBuffer<EntityStore> buffer) -> {
                for (int i = 0; i < chunk_inner.size(); i++) {
                    Ref<EntityStore> npcRef = chunk_inner.getReferenceTo(i);
                    processNPC(npcRef, worldStore, currentTime, playerPositions);
                }
            });

            // Remove stuck NPCs (do this in a separate pass to avoid concurrent modification)
            removeStuckNPCs(worldStore, commandBuffer);
        }

        private void processNPC(Ref<EntityStore> npcRef, Store<EntityStore> store, 
                               long currentTime, List<Vector3d> playerPositions) {
            try {
                // Get NPC component
                NPCEntity npcEntity = store.getComponent(npcRef, NPCEntity.getComponentType());
                if (npcEntity == null) {
                    return;
                }

                // Get UUID for tracking
                UUID npcUuid = npcEntity.getUuid();
                if (npcUuid == null) {
                    return;
                }

                // Get transform for position
                TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
                if (transform == null) {
                    return;
                }
                Vector3d currentPosition = transform.getPosition();

                // Check if NPC has an active target (only track NPCs with targets)
                boolean hasTarget = hasActiveTarget(npcEntity, store);
                if (!hasTarget) {
                    // Remove from tracking if no target
                    World world = WorldUtil.getGameWorld();
                    trackedNPCs.remove(npcUuid);
                    return;
                }

                // Check if NPC is near any player (exempt if within 30 blocks)
                if (isNearPlayer(currentPosition, playerPositions)) {
                    // Don't track NPCs near players
                    World world = WorldUtil.getGameWorld();
                    trackedNPCs.remove(npcUuid);
                    return;
                }

                // Update or create tracking history
                NPCPositionHistory history = trackedNPCs.get(npcUuid);
                if (history == null) {
                    // First time seeing this NPC with a target
                    history = new NPCPositionHistory(currentPosition, currentTime, npcUuid, npcRef);
                    trackedNPCs.put(npcUuid, history);
                    World world = WorldUtil.getGameWorld();
                } else {
                    // Update position
                    history.updatePosition(currentPosition, currentTime);

                    // Check if stuck
                    if (history.hasBeenTrackedFor30Seconds(currentTime)) {
                        double distanceMoved = history.getDistanceMovedIn30Seconds();
                        World world = WorldUtil.getGameWorld();
                        if (world != null) {
                            sendDebugToAllPlayers(world, "[Stuck NPC Debug] NPC " + npcUuid.toString().substring(0, 8) + "... tracked for 30s, moved " + String.format("%.2f", distanceMoved) + " blocks (threshold: " + STUCK_DISTANCE_THRESHOLD + ")");
                        }
                        if (distanceMoved <= STUCK_DISTANCE_THRESHOLD) {
                            // Mark for removal
                            entitiesToRemove.add(npcUuid);
                        }
                        history.resetReferencePoint();
                    }
                }
            } catch (Exception e) {
                // Silently continue if there's any issue processing this NPC
            }
        }

        private boolean hasActiveTarget(NPCEntity npcEntity, Store<EntityStore> store) {
            try {
                if (npcEntity.getRole() == null) {
                    return false;
                }
                MarkedEntitySupport markedEntitySupport = npcEntity.getRole().getMarkedEntitySupport();
                if (markedEntitySupport == null) {
                    return false;
                }
                // Check if NPC has a marked target in the default target slot
                Ref<EntityStore> target = markedEntitySupport.getMarkedEntityRef(MarkedEntitySupport.DEFAULT_TARGET_SLOT);
                if (target == null) {
                    return false;
                }

                // Only count target if it's a player
                Player targetPlayer = store.getComponent(target, Player.getComponentType());
                
                // If target exists but is NOT a player, clear the invalid target
                if (targetPlayer == null) {
                    try {
                        // Clear the invalid target by setting to null
                        markedEntitySupport.setMarkedEntity(MarkedEntitySupport.DEFAULT_TARGET_SLOT, null);
                        World world = WorldUtil.getGameWorld();
                        if (world != null) {
                            sendDebugToAllPlayers(world, "[Stuck NPC Debug] Cleared invalid (non-player) target from NPC " + npcEntity.getUuid().toString().substring(0, 8));
                        }
                    } catch (Exception clearEx) {
                        // Failed to clear, silently continue
                    }
                    return false;
                }
                
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private boolean isNearPlayer(Vector3d npcPosition, List<Vector3d> playerPositions) {
            for (Vector3d playerPos : playerPositions) {
                double dx = npcPosition.x - playerPos.x;
                double dy = npcPosition.y - playerPos.y;
                double dz = npcPosition.z - playerPos.z;
                double distanceSquared = dx * dx + dy * dy + dz * dz;
                
                if (distanceSquared <= PLAYER_PROXIMITY_THRESHOLD * PLAYER_PROXIMITY_THRESHOLD) {
                    return true;
                }
            }
            return false;
        }

        private List<Vector3d> getPlayerPositions() {
            World world = WorldUtil.getGameWorld();
            if (world == null) {
                return List.of();
            }

            List<Player> players = world.getPlayers();
            return players.stream()
                .map(player -> {
                    EntityStore entityStore = world.getEntityStore();
                    Store<EntityStore> store = entityStore.getStore();
                    Ref<EntityStore> playerRef = player.getReference();
                    TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
                    return transform != null ? transform.getPosition() : null;
                })
                .filter(pos -> pos != null)
                .toList();
        }

        private void removeStuckNPCs(Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
            if (entitiesToRemove.isEmpty()) {
                return;
            }

            World world = WorldUtil.getGameWorld();
            if (world == null) {
                entitiesToRemove.clear();
                return;
            }
            
            Set<UUID> toRemove = new HashSet<>(entitiesToRemove);
            entitiesToRemove.clear();

            // Queue removals directly on the command buffer (thread-safe within system tick)
            for (UUID uuid : toRemove) {
                try {
                    NPCPositionHistory history = trackedNPCs.get(uuid);
                    if (history == null || history.entityRef == null) {
                        continue;
                    }

                    commandBuffer.tryRemoveEntity(history.entityRef, RemoveReason.REMOVE);
                    
                    // Remove from tracking
                    trackedNPCs.remove(uuid);
                } catch (Exception e) {
                    sendDebugToAllPlayers(world, "[Stuck NPC Debug] Removal failed for " + uuid.toString() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                }
            }
        }

        private void sendDebugToAllPlayers(World world, String message) {
            try {
                if (!DebugMessageSettings.areDebugMessagesEnabled()) {
                    return;
                }
                List<Player> players = world.getPlayers();
                for (Player player : players) {
                    player.sendMessage(Message.raw(message));
                }
            } catch (Exception e) {
                // Silently fail if message sending fails
            }
        }
    }
}
