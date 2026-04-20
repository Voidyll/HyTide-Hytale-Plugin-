package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.SpawnDataManager;
import me.voidyll.data.SpawnMarkerData;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;

public class ListSpawnsCommand extends CommandBase {
    private final SpawnDataManager dataManager;

    public ListSpawnsCommand(SpawnDataManager dataManager) {
        super("list-spawns", "Lists all spawn markers");
        this.dataManager = dataManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        List<SpawnMarkerData> markers = dataManager.loadSpawnMarkers();

        if (markers.isEmpty()) {
            context.sendMessage(Message.raw("No spawn markers defined."));
            return;
        }

        context.sendMessage(Message.raw("Spawn Markers:"));
        for (SpawnMarkerData marker : markers) {
            String id = marker.getId() != null ? marker.getId() : "no-id";
            String entity = marker.getEntityType() != null && !marker.getEntityType().isEmpty()
                    ? marker.getEntityType() : "random";
            String line = "- %s | entity=%s | count=%d | group=%d | role=%s | (%.1f, %.1f, %.1f) | world=%s"
                    .formatted(id, entity, marker.getSpawnNumber(), marker.getGroupNumber(),
                              marker.getRole(), marker.getX(), marker.getY(), marker.getZ(), marker.getWorldName());
            context.sendMessage(Message.raw(line));
            
            if (marker.getIdentifier() != null && !marker.getIdentifier().isEmpty()) {
                context.sendMessage(Message.raw("    identifier=%s".formatted(marker.getIdentifier())));
            }
            if (marker.getPathName() != null && !marker.getPathName().isEmpty()) {
                context.sendMessage(Message.raw("    pathName=%s".formatted(marker.getPathName())));
            }
        }
    }
}
