package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.ItemSpawnDataManager;
import me.voidyll.data.ItemSpawnMarkerData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;

public class ListItemSpawnsCommand extends CommandBase {
    private final ItemSpawnDataManager dataManager;

    public ListItemSpawnsCommand(ItemSpawnDataManager dataManager) {
        super("list-item-spawns", "Lists all item spawn markers");
        this.dataManager = dataManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        List<ItemSpawnMarkerData> markers = dataManager.getMarkers();

        if (markers.isEmpty()) {
            context.sendMessage(Message.raw("No item spawn markers defined."));
            return;
        }

        context.sendMessage(Message.raw("Item Spawn Markers:"));
        for (ItemSpawnMarkerData marker : markers) {
            String id = marker.getId() != null ? marker.getId() : "no-id";
            context.sendMessage(Message.raw(
                "- %s | (%.1f, %.1f, %.1f) | world=%s"
                    .formatted(id, marker.getX(), marker.getY(), marker.getZ(), marker.getWorldName())
            ));
        }
    }
}
