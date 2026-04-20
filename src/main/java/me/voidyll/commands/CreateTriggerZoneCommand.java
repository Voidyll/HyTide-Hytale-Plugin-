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
import me.voidyll.data.TriggerZoneData;
import me.voidyll.data.TriggerZoneManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CreateTriggerZoneCommand extends CommandBase {
    private final RequiredArg<String> ZONE_NAME;
    private final RequiredArg<Integer> GROUP_NUMBER;
    private final RequiredArg<Double> RADIUS;
    private final RequiredArg<String> TYPE;
    private final com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg<String> IDENTIFIER;

    private final TriggerZoneManager zoneManager;

    public CreateTriggerZoneCommand(TriggerZoneManager zoneManager) {
        super("create-trigger-zone", "Creates a proximity trigger zone at your current location");
        this.zoneManager = zoneManager;

        this.ZONE_NAME = withRequiredArg("zoneName", "Name of the trigger zone", ArgTypes.STRING);
        this.GROUP_NUMBER = withRequiredArg("groupNumber", "Spawn group number to trigger", ArgTypes.INTEGER);
        this.RADIUS = withRequiredArg("radius", "Radius of the zone (blocks)", ArgTypes.DOUBLE);
        this.TYPE = withRequiredArg("type", "Trigger type (checkpoint, ambient, boss, patrol, special)", ArgTypes.STRING);
        this.IDENTIFIER = withOptionalArg("identifier", "Optional identifier to link with a spawn marker", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command can only be used by players."));
            return;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        String zoneName = ZONE_NAME.get(context);
        int groupNumber = GROUP_NUMBER.get(context);
        double radius = RADIUS.get(context);
        String type = normalizeType(TYPE.get(context));
        String identifier = IDENTIFIER.get(context);

        if (!isValidType(type)) {
            context.sendMessage(Message.raw("Error: Invalid trigger type. Use one of: checkpoint, ambient, boss, patrol, special."));
            return;
        }

        World targetWorld = Universe.get().getDefaultWorld();
        if (targetWorld == null) {
            context.sendMessage(Message.raw("Error: No world available."));
            return;
        }

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

            TriggerZoneData zone = new TriggerZoneData(
                zoneName,
                groupNumber,
                radius,
                position.x,
                position.y,
                position.z,
                worldName,
                type,
                identifier
            );

            zoneManager.addZone(zone);

            context.sendMessage(Message.raw("Trigger zone created!"));
            context.sendMessage(Message.raw("  Name: %s".formatted(zoneName)));
            context.sendMessage(Message.raw("  Type: %s".formatted(type)));
            if (identifier != null && !identifier.isEmpty()) {
                context.sendMessage(Message.raw("  Identifier: %s".formatted(identifier)));
            }
            context.sendMessage(Message.raw("  Group: %d".formatted(groupNumber)));
            context.sendMessage(Message.raw("  Radius: %.2f".formatted(radius)));
            context.sendMessage(Message.raw("  Position: %.2f, %.2f, %.2f".formatted(position.x, position.y, position.z)));
            context.sendMessage(Message.raw("  World: %s".formatted(worldName)));
        });
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toLowerCase();
    }

    private boolean isValidType(String type) {
        return type.equals("checkpoint") || type.equals("ambient") || type.equals("boss") || type.equals("patrol") || type.equals("special");
    }
}
