package me.voidyll.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.voidyll.data.SpawnDataManager;
import me.voidyll.data.SpawnMarkerData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CreateSpawnCommand extends CommandBase {
    private final RequiredArg<String> SPAWN_ID;
    private final com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg<String> ENTITY_TYPE;
    private final RequiredArg<Integer> SPAWN_NUMBER;
    private final RequiredArg<Integer> GROUP_NUMBER;
    private final RequiredArg<String> ROLE;
    private final com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg<String> IDENTIFIER;
    private final com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg<String> PATH_NAME;

    private final SpawnDataManager dataManager;

    public CreateSpawnCommand(SpawnDataManager dataManager) {
        super("create-spawn", "Creates a spawn marker at your current location");
        this.dataManager = dataManager;
        
        this.SPAWN_ID = withRequiredArg("id", "Unique ID for this spawn marker", ArgTypes.STRING);
        this.ENTITY_TYPE = withOptionalArg("entityType", "Optional specific entity type (if not set, uses random selection)", ArgTypes.STRING);
        this.SPAWN_NUMBER = withRequiredArg("spawnNumber", "Number of entities to spawn", ArgTypes.INTEGER);
        this.GROUP_NUMBER = withRequiredArg("groupNumber", "Group number for this spawn marker", ArgTypes.INTEGER);
        this.ROLE = withRequiredArg("role", "Spawn role (horde, ambient, boss, patrol, special)", ArgTypes.STRING);
        this.IDENTIFIER = withOptionalArg("identifier", "Optional identifier to link with a trigger", ArgTypes.STRING);
        this.PATH_NAME = withOptionalArg("pathName", "Optional path name for patrol NPCs to follow", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command can only be used by players."));
            return;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        
        // Get command arguments on the command thread (safe - no component access yet)
        String spawnId = SPAWN_ID.get(context);
        String entityType = ENTITY_TYPE.get(context);
        int spawnNumber = SPAWN_NUMBER.get(context);
        int groupNumber = GROUP_NUMBER.get(context);
        String role = normalizeRole(ROLE.get(context));
        String identifier = IDENTIFIER.get(context);
        String pathName = PATH_NAME.get(context);

        if (!isValidRole(role)) {
            context.sendMessage(Message.raw("Error: Invalid role. Use one of: horde, ambient, boss, patrol, special."));
            return;
        }

        if (dataManager.hasMarkerId(spawnId)) {
            context.sendMessage(Message.raw("Error: A spawn marker with ID '" + spawnId + "' already exists."));
            return;
        }
        
        // Try to get the world by iterating through all worlds
        // This is a workaround for accessing world without component access
        // We'll try to find the player in each world, or use the default world
        World targetWorld = Universe.get().getDefaultWorld();
        
        if (targetWorld == null) {
            context.sendMessage(Message.raw("Error: No world available."));
            return;
        }
        
        // Now defer ALL Store and component access to the target world's thread
        targetWorld.execute(() -> {
            // NOW we're on the world thread, safe to access Store
            Store<EntityStore> store = playerRef.getStore();
            
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player == null) {
                context.sendMessage(Message.raw("Error: Could not retrieve player data."));
                return;
            }

            TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
            if (transform == null) {
                context.sendMessage(Message.raw("Error: Could not retrieve player position."));
                return;
            }

            Vector3d position = transform.getPosition();
            World playerWorld = player.getWorld();
            String worldName = playerWorld != null ? playerWorld.getName() : "unknown";

            SpawnMarkerData marker = new SpawnMarkerData(
                entityType,
                spawnNumber,
                groupNumber,
                role,
                position.x,
                position.y,
                position.z,
                worldName,
                identifier
            );
            
            // Set path name if provided
            if (pathName != null && !pathName.isEmpty()) {
                marker.setPathName(pathName);
            }
            marker.setId(spawnId);

            dataManager.addSpawnMarker(marker);

            context.sendMessage(Message.raw("Spawn marker created!"));
            context.sendMessage(Message.raw("  ID: %s".formatted(spawnId)));
            if (entityType != null && !entityType.isEmpty()) {
                context.sendMessage(Message.raw("  Entity: %s (fixed)".formatted(entityType)));
            } else {
                context.sendMessage(Message.raw("  Entity: Random selection based on role"));
            }
            context.sendMessage(Message.raw("  Count: %d".formatted(spawnNumber)));
            context.sendMessage(Message.raw("  Group: %d".formatted(groupNumber)));
            context.sendMessage(Message.raw("  Role: %s".formatted(role)));
            if (identifier != null && !identifier.isEmpty()) {
                context.sendMessage(Message.raw("  Identifier: %s".formatted(identifier)));
            }
            if (pathName != null && !pathName.isEmpty()) {
                context.sendMessage(Message.raw("  Path Name: %s".formatted(pathName)));
            }
            context.sendMessage(Message.raw("  Position: %.2f, %.2f, %.2f".formatted(position.x, position.y, position.z)));
            context.sendMessage(Message.raw("  World: %s".formatted(worldName)));
        });
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }

    private boolean isValidRole(String role) {
        return role.equals("horde") || role.equals("ambient") || role.equals("boss") || role.equals("patrol") || role.equals("special");
    }
}
