package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.ItemSpawnDataManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ShowItemSpawnsDataLocationCommand extends CommandBase {
    private final ItemSpawnDataManager dataManager;

    public ShowItemSpawnsDataLocationCommand(ItemSpawnDataManager dataManager) {
        super("show-item-spawns-data-location", "Shows the file path for item spawns data");
        this.dataManager = dataManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String filePath = dataManager.getDataFilePath().toAbsolutePath().toString();
        
        context.sendMessage(Message.raw("Item spawns data file location:"));
        context.sendMessage(Message.raw("%s".formatted(filePath)));
        context.sendMessage(Message.raw("You can edit this file to manually change item spawn point data."));
        context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
    }
}
