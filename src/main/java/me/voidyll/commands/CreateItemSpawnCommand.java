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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import me.voidyll.utils.WorldUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.voidyll.data.ItemSpawnDataManager;
import me.voidyll.data.ItemSpawnMarkerData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CreateItemSpawnCommand extends CommandBase {
    
    private final RequiredArg<String> SPAWN_ID;
    private final ItemSpawnDataManager dataManager;

    public CreateItemSpawnCommand(ItemSpawnDataManager dataManager) {
        super("create-item-spawn", "Creates an item spawn marker at your current location");
        this.dataManager = dataManager;
        this.SPAWN_ID = withRequiredArg("id", "Unique ID for this item spawn marker", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command can only be used by players."));
            return;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        
        String spawnId = SPAWN_ID.get(context);

        if (dataManager.hasMarkerId(spawnId)) {
            context.sendMessage(Message.raw("Error: An item spawn marker with ID '" + spawnId + "' already exists."));
            return;
        }
        
        World targetWorld = WorldUtil.getPlayerWorld(context.sender().getUuid());
        if (targetWorld == null) {
            context.sendMessage(Message.raw("Error: No world available."));
            return;
        }
        
        // Defer all Store and component access to the world thread
        targetWorld.execute(() -> {
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
            
            ItemSpawnMarkerData marker = new ItemSpawnMarkerData(
                position.x,
                position.y,
                position.z,
                worldName
            );
            marker.setId(spawnId);

            dataManager.addMarker(marker);

            context.sendMessage(Message.raw(String.format(
                "Item spawn marker '%s' created at X: %.2f, Y: %.2f, Z: %.2f in world '%s'",
                spawnId, position.x, position.y, position.z, worldName
            )));
        });
    }
}
