package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.SpawnDataManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DeleteSpawnCommand extends CommandBase {
    private final RequiredArg<String> SPAWN_ID;
    private final SpawnDataManager dataManager;

    public DeleteSpawnCommand(SpawnDataManager dataManager) {
        super("delete-spawn", "Deletes a spawn marker by ID");
        this.dataManager = dataManager;
        this.SPAWN_ID = withRequiredArg("id", "The ID of the spawn marker to delete", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String spawnId = SPAWN_ID.get(context);

        if (dataManager.removeMarkerById(spawnId)) {
            context.sendMessage(Message.raw("Spawn marker '" + spawnId + "' deleted."));
        } else {
            context.sendMessage(Message.raw("Spawn marker '" + spawnId + "' not found."));
        }
    }
}
