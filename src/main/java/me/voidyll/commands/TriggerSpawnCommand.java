package me.voidyll.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.entities.PathManager;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import me.voidyll.data.ActiveSpawnGroupManager;
import me.voidyll.data.RoleConfigManager;
import me.voidyll.data.RoleDefinition;
import me.voidyll.data.RoleSelector;
import me.voidyll.data.SpawnDataManager;
import me.voidyll.data.SpawnMarkerData;
import me.voidyll.utils.WorldUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;

public class TriggerSpawnCommand extends CommandBase {
    private final RequiredArg<String> GROUP_NUMBERS;
    private final RequiredArg<String> ROLE;

    private final SpawnDataManager dataManager;
    private final ActiveSpawnGroupManager groupManager;
    private final RoleConfigManager roleConfigManager;

    public TriggerSpawnCommand(SpawnDataManager dataManager, ActiveSpawnGroupManager groupManager, RoleConfigManager roleConfigManager) {
        super("trigger-spawn", "Triggers spawning for specified group numbers and role (e.g., 1,2,3 horde)");
        this.dataManager = dataManager;
        this.groupManager = groupManager;
        this.roleConfigManager = roleConfigManager;
        
        this.GROUP_NUMBERS = withRequiredArg("groupNumbers", "Comma-separated group numbers (e.g., 1,2,3)", ArgTypes.STRING);
        this.ROLE = withRequiredArg("role", "Spawn role (horde, ambient, boss, patrol, special)", ArgTypes.STRING);
    }

    /**
     * Public method for programmatic spawning (e.g., from spawn director).
     * Bypasses active group validation and spawns entities directly.
     * 
     * @param groupNumbers List of group numbers to spawn
     * @param role Spawn role type
     * @param playerTarget Optional player reference to mark as target (can be null)
     * @return SpawnResult containing counts of spawned and failed entities
     */
    public SpawnResult spawnForGroups(List<Integer> groupNumbers, String role, Ref<EntityStore> playerTarget) {
        return spawnForGroups(groupNumbers, role, playerTarget, null);
    }

    /**
     * Public method for programmatic spawning with optional identifier filtering.
     * 
     * @param groupNumbers List of group numbers to spawn
     * @param role Spawn role type
     * @param playerTarget Optional player reference to mark as target (can be null)
     * @param identifier Optional identifier to match specific markers (null for no filtering)
     * @return SpawnResult containing counts of spawned and failed entities
     */
    public SpawnResult spawnForGroups(List<Integer> groupNumbers, String role, Ref<EntityStore> playerTarget, String identifier) {
        int totalSpawned = 0;
        int failedSpawns = 0;

        List<SpawnMarkerData> markers;
        if (identifier != null && !identifier.isEmpty()) {
            markers = dataManager.getMarkersByGroupsRoleAndIdentifier(groupNumbers, role, identifier);
        } else {
            markers = dataManager.getMarkersByGroupsAndRole(groupNumbers, role);
        }
        
        if (markers.isEmpty()) {
            String msg = "No spawn markers found for groups: " + groupNumbers.toString() + " with role: " + role;
            if (identifier != null && !identifier.isEmpty()) {
                msg += " and identifier: " + identifier;
            }
            return new SpawnResult(0, 0, msg);
        }

        World targetWorld = WorldUtil.getGameWorld();
        if (targetWorld == null) {
            return new SpawnResult(0, 0, "No world available");
        }

        // Initialize role selector for this spawn session
        // Caps are shared across all markers in this call
        boolean isPatrol = "patrol".equalsIgnoreCase(role);
        List<RoleDefinition> availableRoles = roleConfigManager.getRolesForType(role);
        RoleSelector roleSelector = new RoleSelector(availableRoles, isPatrol);

        // Process each marker
        for (SpawnMarkerData marker : markers) {
            World spawnWorld = Universe.get().getWorld(marker.getWorldName());
            if (spawnWorld == null) {
                spawnWorld = targetWorld;
            }

            if (spawnWorld == null) {
                failedSpawns += marker.getSpawnNumber();
                continue;
            }

            final World finalSpawnWorld = spawnWorld;

            // Execute spawning on the world thread
            finalSpawnWorld.execute(() -> {
                performSpawn(marker, finalSpawnWorld, playerTarget, roleSelector);
            });
        }

        return new SpawnResult(totalSpawned, failedSpawns, null);
    }

    private void performSpawn(SpawnMarkerData marker, World spawnWorld, Ref<EntityStore> playerTarget, RoleSelector roleSelector) {
        Vector3d position = new Vector3d(marker.getX(), marker.getY(), marker.getZ());
        Vector3f rotation = new Vector3f(0f, 0f, 0f);

        // Check if marker has a specific entity type set (legacy/fixed entity behavior)
        boolean hasFixedEntity = marker.getEntityType() != null && !marker.getEntityType().trim().isEmpty();

        // For patrol spawns without a fixed entity, select the patrol role once per marker
        RoleSelector markerRoleSelector = roleSelector;
        if (!hasFixedEntity && "patrol".equalsIgnoreCase(marker.getRole())) {
            // Create a new selector for this specific patrol marker
            List<RoleDefinition> availableRoles = roleConfigManager.getRolesForType(marker.getRole());
            markerRoleSelector = new RoleSelector(availableRoles, true);
        }

        for (int i = 0; i < marker.getSpawnNumber(); i++) {
            try {
                EntityStore entityStore = spawnWorld.getEntityStore();
                Store<EntityStore> worldStore = entityStore.getStore();

                // Determine which entity to spawn
                String entityToSpawn;
                if (hasFixedEntity) {
                    // Use the fixed entity type from the marker
                    entityToSpawn = marker.getEntityType();
                } else {
                    // Use role selector to pick an entity based on weights and caps
                    entityToSpawn = markerRoleSelector.selectNextRole();
                    if (entityToSpawn == null) {
                        // All caps reached, stop spawning
                        sendDebug(playerTarget, worldStore, "All role caps reached, stopping spawn.");
                        break;
                    }
                }

                sendDebug(playerTarget, worldStore, "Spawning NPC: %s at %.2f, %.2f, %.2f (role=%s, pathName=%s)"
                    .formatted(entityToSpawn, position.x, position.y, position.z, marker.getRole(), marker.getPathName()));

                it.unimi.dsi.fastutil.Pair<Ref<EntityStore>, INonPlayerCharacter> result =
                    NPCPlugin.get().spawnNPC(worldStore, entityToSpawn, null, position, rotation);

                if (result == null) {
                    continue;
                }

                Ref<EntityStore> entityRef = result.first();

                // Mark player as target if provided (skip for ambient/patrol spawns)
                if (playerTarget != null && !"ambient".equalsIgnoreCase(marker.getRole()) && !"patrol".equalsIgnoreCase(marker.getRole())) {
                    NPCEntity npcComponent = worldStore.getComponent(entityRef, NPCEntity.getComponentType());
                    if (npcComponent != null && npcComponent.getRole() != null) {
                        npcComponent.getRole().setMarkedTarget(MarkedEntitySupport.DEFAULT_TARGET_SLOT, playerTarget);
                        sendDebug(playerTarget, worldStore, "Marked player as target for spawned NPC.");
                    }
                }

                // Assign path if this is a patrol marker with a path name
                if (marker.getPathName() != null && !marker.getPathName().isEmpty() && "patrol".equalsIgnoreCase(marker.getRole())) {
                    sendDebug(playerTarget, worldStore, "Attempting path assignment: %s".formatted(marker.getPathName()));
                    assignPathToNPC(entityRef, marker.getPathName(), spawnWorld, worldStore, playerTarget);
                } else if ("patrol".equalsIgnoreCase(marker.getRole())) {
                    sendDebug(playerTarget, worldStore, "Patrol marker missing pathName - skipping path assignment.");
                }
            } catch (Exception e) {
                // Silent failure for programmatic use
            }
        }
    }

    private void assignPathToNPC(Ref<EntityStore> entityRef, String pathName, World world, Store<EntityStore> worldStore, Ref<EntityStore> playerTarget) {
        try {
            NPCEntity npcComponent = worldStore.getComponent(entityRef, NPCEntity.getComponentType());
            if (npcComponent == null) {
                sendDebug(playerTarget, worldStore, "NPCEntity component missing - cannot assign path.");
                return;
            }

            // Get the world's path data
            WorldPathData pathData = worldStore.getResource(WorldPathData.getResourceType());
            if (pathData == null) {
                sendDebug(playerTarget, worldStore, "WorldPathData not available - cannot assign path.");
                return;
            }

            // Find the path by iterating through all paths and matching by name
            IPrefabPath matchingPath = null;
            List<IPrefabPath> allPaths = pathData.getAllPrefabPaths();
            sendDebug(playerTarget, worldStore, "Paths in world: " + allPaths.size());
            for (IPrefabPath path : allPaths) {
                sendDebug(playerTarget, worldStore, "Found path: " + path.getName());
                if (pathName.equals(path.getName())) {
                    matchingPath = path;
                    break;
                }
            }

            if (matchingPath != null) {
                // Assign the path to the NPC
                PathManager pathManager = npcComponent.getPathManager();
                pathManager.setPrefabPath(matchingPath.getId(), matchingPath);
                sendDebug(playerTarget, worldStore, "Assigned path: %s (id=%s)".formatted(matchingPath.getName(), matchingPath.getId()));
            } else {
                sendDebug(playerTarget, worldStore, "No path matched name: " + pathName);
            }
        } catch (Exception e) {
            // Silent failure - path assignment is optional
        }
    }

    private void sendDebug(Ref<EntityStore> playerTarget, Store<EntityStore> worldStore, String message) {
        if (playerTarget == null) {
            return;
        }

        Player player = worldStore.getComponent(playerTarget, Player.getComponentType());
        if (player == null) {
            return;
        }

        player.sendMessage(Message.raw("[DEBUG] " + message));
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String groupNumbersStr = GROUP_NUMBERS.get(context);
        String role = normalizeRole(ROLE.get(context));
        
        // Get player reference from context
        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        if (playerRef == null) {
            context.sendMessage(Message.raw("Error: This command must be run by a player."));
            return;
        }
        
        // Parse comma-separated group numbers (safe on command thread)
        List<Integer> groupNumbers = new ArrayList<>();
        try {
            String[] parts = groupNumbersStr.split(",");
            for (String part : parts) {
                groupNumbers.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException e) {
            context.sendMessage(Message.raw("Error: Invalid group numbers. Use comma-separated integers (e.g., 1,2,3)"));
            return;
        }

        if (groupNumbers.isEmpty()) {
            context.sendMessage(Message.raw("Error: No group numbers provided."));
            return;
        }

        if (!isValidRole(role)) {
            context.sendMessage(Message.raw("Error: Invalid role. Use one of: horde, ambient, boss, patrol, special."));
            return;
        }

        // Get world (thread-safe operation)
        World targetWorld = WorldUtil.getPlayerWorld(context.sender().getUuid());
        if (targetWorld == null) {
            context.sendMessage(Message.raw("Error: No world available."));
            return;
        }

        // Defer component access to world thread
        targetWorld.execute(() -> {
            try {
                // NOW we're on the world thread, safe to access components
                Store<EntityStore> store = playerRef.getStore();
                com.hypixel.hytale.server.core.entity.entities.Player player = 
                    store.getComponent(playerRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                
                if (player == null) {
                    context.sendMessage(Message.raw("Error: Could not retrieve player data."));
                    return;
                }
                
                String playerKey = player.getUuid().toString();
                
                // Check active group and validate
                Integer activeGroup = groupManager.getActiveGroup(playerKey);
                
                context.sendMessage(Message.raw("[DEBUG] Player UUID: " + playerKey));
                context.sendMessage(Message.raw("[DEBUG] Active group: " + (activeGroup != null ? activeGroup.toString() : "NONE")));
                context.sendMessage(Message.raw("[DEBUG] Requested groups: " + groupNumbers.toString()));
                context.sendMessage(Message.raw("[DEBUG] Role: " + role));
                
                if (activeGroup == null) {
                    context.sendMessage(Message.raw("Warning: You have not entered any trigger zone. No active group set."));
                    return;
                }
                
                // Check if any requested groups match active group
                if (!groupNumbers.contains(activeGroup)) {
                    context.sendMessage(Message.raw("Cannot spawn group(s) %s - your active group is %d. Enter the correct trigger zone first."
                        .formatted(groupNumbers.toString(), activeGroup)));
                    return;
                }
                
                context.sendMessage(Message.raw("[DEBUG] Validation passed - spawning!"));
                
                // Perform the spawn using the public method
                spawnForGroups(groupNumbers, role, playerRef);
            } catch (Exception e) {
                context.sendMessage(Message.raw("Error: " + e.getMessage()));
            }
        });
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }

    private boolean isValidRole(String role) {
        return role.equals("horde") || role.equals("ambient") || role.equals("boss") || role.equals("patrol") || role.equals("special");
    }

    // Result class for programmatic access
    public static class SpawnResult {
        private final int spawned;
        private final int failed;
        private final String errorMessage;

        public SpawnResult(int spawned, int failed, String errorMessage) {
            this.spawned = spawned;
            this.failed = failed;
            this.errorMessage = errorMessage;
        }

        public int getSpawned() {
            return spawned;
        }

        public int getFailed() {
            return failed;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean hasError() {
            return errorMessage != null;
        }
    }
}
