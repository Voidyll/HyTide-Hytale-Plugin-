package me.voidyll.commands;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import me.voidyll.utils.WorldUtil;
import me.voidyll.data.ActivatedTriggersManager;
import me.voidyll.data.ActiveSpawnGroupManager;
import me.voidyll.data.BlockInteractTriggerManager;
import me.voidyll.data.TriggerZoneData;
import me.voidyll.data.TriggerZoneManager;
import me.voidyll.systems.EventHandler;
import me.voidyll.systems.ItemSpawnSystem;
import me.voidyll.systems.SpawnDirectorSystem;
import me.voidyll.systems.StuckNPCCleanupSystem;
import me.voidyll.utils.CommandExecutor;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class ResetCommand extends CommandBase {
    private final ActiveSpawnGroupManager groupManager;
    private final ActivatedTriggersManager triggersManager;
    private final BlockInteractTriggerManager blockInteractTriggerManager;
    private final TriggerZoneManager triggerZoneManager;
    private final SpawnDirectorSystem spawnDirector;
    private final EventHandler eventHandler;
    private final ItemSpawnSystem itemSpawnSystem;
    private final Random random = new Random();

    public ResetCommand(ActiveSpawnGroupManager groupManager, ActivatedTriggersManager activatedTriggersManager,
                        BlockInteractTriggerManager blockInteractTriggerManager, TriggerZoneManager triggerZoneManager,
                        SpawnDirectorSystem spawnDirector, EventHandler eventHandler, ItemSpawnSystem itemSpawnSystem) {
        super("reset", "Resets the world state (clean entities, teleport players, reset active groups and triggers)");
        this.groupManager = groupManager;
        this.triggersManager = activatedTriggersManager;
        this.blockInteractTriggerManager = blockInteractTriggerManager;
        this.triggerZoneManager = triggerZoneManager;
        this.spawnDirector = spawnDirector;
        this.eventHandler = eventHandler;
        this.itemSpawnSystem = itemSpawnSystem;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        context.sendMessage(Message.raw("Resetting world..."));

        // Reset door states and block changes on the command thread (CommandExecutor must not
        // run from inside a world.execute() callback to avoid deadlocks)
        if (eventHandler != null) {
            try {
                eventHandler.resetDoorStates();
                context.sendMessage(Message.raw("Door states reset."));
            } catch (Exception e) {
                context.sendMessage(Message.raw("Failed to reset doors: " + e.getMessage()));
            }
            
            try {
                eventHandler.resetBlockChanges();
                context.sendMessage(Message.raw("Block changes restored."));
            } catch (Exception e) {
                context.sendMessage(Message.raw("Failed to restore blocks: " + e.getMessage()));
            }
        }

        World targetWorld = WorldUtil.getPlayerWorld(context.sender().getUuid());
        if (targetWorld == null) {
            context.sendMessage(Message.raw("Error: No world available."));
            return;
        }

        targetWorld.execute(() -> {
            try {
                // Remove all NPCs directly via the entity store (safe on world thread)
                EntityStore entityStore = targetWorld.getEntityStore();
                Store<EntityStore> worldStore = entityStore.getStore();
                worldStore.forEachChunk(
                    NPCEntity.getComponentType(),
                    (ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> buffer) -> {
                        for (int i = 0; i < chunk.size(); i++) {
                            buffer.tryRemoveEntity(chunk.getReferenceTo(i), RemoveReason.REMOVE);
                        }
                    }
                );
                context.sendMessage(Message.raw("NPCs cleaned."));

                List<Player> players = targetWorld.getPlayers();

                groupManager.clear();
                triggersManager.clearAllStates();
                blockInteractTriggerManager.clearAll();

                // Stop current event if running and reset block trigger states
                if (eventHandler != null) {
                    if (eventHandler.getCurrentEvent() != null) {
                        eventHandler.stopCurrentEvent();
                    }
                    eventHandler.resetTriggerBlockStates();
                    
                    // Clear tracked door states (thread-safe operation)
                    eventHandler.clearTrackedDoorStates();
                    
                    // Clear tracked block changes
                    eventHandler.clearTrackedBlockChanges();
                }

                // Restart spawn director timers
                if (spawnDirector != null) {
                    StuckNPCCleanupSystem.resetTotalNpcCount();
                    spawnDirector.restartHordeTimer();
                    spawnDirector.restartSpecialTimer();
                    spawnDirector.unpauseHordeTimer();
                    spawnDirector.unpauseSpecialTimer();
                }

                // Remove all dropped items and spawn new ones at markers
                if (itemSpawnSystem != null) {
                    itemSpawnSystem.removeAllDroppedItems();
                    itemSpawnSystem.spawnItemsAtMarkers();
                }

                // Process boss/patrol triggers
                processBossPatrolTriggers();

                for (Player player : players) {
                    UUID playerUuid = player.getUuid();
                    PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
                    
                    if (playerRef != null) {
                        String playerName = playerRef.getUsername();
                        // Use async to avoid blocking the world thread (sync would deadlock)
                        CommandExecutor.executeCommand("spawn " + playerName);
                    }
                    
                    groupManager.setActiveGroup(playerUuid.toString(), 0);
                    player.sendMessage(Message.raw("You have been reset to world spawn. Active group set to 0."));
                }

                context.sendMessage(Message.raw("Reset complete. All active groups and trigger states cleared."));
            } catch (Exception e) {
                context.sendMessage(Message.raw("Reset failed: " + e.getMessage()));
            }
        });
    }

    private void processBossPatrolTriggers() {
        List<TriggerZoneData> allZones = triggerZoneManager.loadZones();
        
        // Identify danger groups (groups with at least one boss or patrol trigger)
        Map<Integer, List<TriggerZoneData>> groupTriggers = new HashMap<>();
        for (TriggerZoneData zone : allZones) {
            String type = zone.getType() != null ? zone.getType().toLowerCase() : "";
            if (type.equals("boss") || type.equals("patrol")) {
                groupTriggers.computeIfAbsent(zone.getGroupNumber(), k -> new ArrayList<>()).add(zone);
            }
        }

        if (groupTriggers.isEmpty()) {
            return;
        }

        // Sort group numbers for consistent processing
        List<Integer> dangerGroups = new ArrayList<>(groupTriggers.keySet());
        Collections.sort(dangerGroups);

        // Start with random type: 0 = boss, 1 = patrol
        boolean isBossType = random.nextBoolean();

        // Phase 1: Disable opposite type for each group, alternating
        for (Integer groupNumber : dangerGroups) {
            List<TriggerZoneData> triggersInGroup = groupTriggers.get(groupNumber);
            String desiredType = isBossType ? "boss" : "patrol";
            String oppositeType = isBossType ? "patrol" : "boss";

            // Check if this group has any triggers of the desired type
            boolean hasDesiredType = triggersInGroup.stream()
                .anyMatch(z -> z.getType().toLowerCase().equals(desiredType));

            // Disable all opposite type triggers
            List<String> oppositeTriggersToDisable = triggersInGroup.stream()
                .filter(z -> z.getType().toLowerCase().equals(oppositeType))
                .map(TriggerZoneData::getName)
                .collect(Collectors.toList());
            
            triggersManager.setDisabledBatch(oppositeTriggersToDisable);

            // Only flip type if this group had triggers of the desired type
            if (hasDesiredType) {
                isBossType = !isBossType;
            }
            // If no desired type triggers exist, keep the same type for next group
        }

        // Phase 2: For each group, randomly select one remaining active trigger
        for (Integer groupNumber : dangerGroups) {
            List<TriggerZoneData> triggersInGroup = groupTriggers.get(groupNumber);
            
            // Get remaining active triggers (not disabled)
            List<TriggerZoneData> activeTriggers = triggersInGroup.stream()
                .filter(z -> !triggersManager.isDisabled(z.getName()))
                .collect(Collectors.toList());

            if (activeTriggers.isEmpty()) {
                continue;
            }

            // Randomly select one to keep active, disable all others
            TriggerZoneData selectedTrigger = activeTriggers.get(random.nextInt(activeTriggers.size()));
            
            List<String> toDisable = activeTriggers.stream()
                .filter(z -> !z.getName().equals(selectedTrigger.getName()))
                .map(TriggerZoneData::getName)
                .collect(Collectors.toList());
            
            triggersManager.setDisabledBatch(toDisable);
        }
    }
}

