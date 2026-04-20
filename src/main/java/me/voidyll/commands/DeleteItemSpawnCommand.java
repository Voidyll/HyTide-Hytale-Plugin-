package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.ItemSpawnDataManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DeleteItemSpawnCommand extends CommandBase {
    private final RequiredArg<String> SPAWN_ID;
    private final ItemSpawnDataManager dataManager;

    public DeleteItemSpawnCommand(ItemSpawnDataManager dataManager) {
        super("delete-item-spawn", "Deletes an item spawn marker by ID");
        this.dataManager = dataManager;
        this.SPAWN_ID = withRequiredArg("id", "The ID of the item spawn marker to delete", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String spawnId = SPAWN_ID.get(context);

        if (dataManager.removeMarkerById(spawnId)) {
            context.sendMessage(Message.raw("Item spawn marker '" + spawnId + "' deleted."));
        } else {
            context.sendMessage(Message.raw("Item spawn marker '" + spawnId + "' not found."));
        }
    }
}
