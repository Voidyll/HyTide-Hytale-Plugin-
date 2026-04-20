package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.SpawnDataManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ShowSpawnMarkersDataLocationCommand extends CommandBase {
    private final SpawnDataManager dataManager;

    public ShowSpawnMarkersDataLocationCommand(SpawnDataManager dataManager) {
        super("show-spawn-markers-data-location", "Shows the file path for spawn markers data");
        this.dataManager = dataManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String filePath = dataManager.getDataFilePath().toAbsolutePath().toString();
        
        context.sendMessage(Message.raw("Spawn markers data file location:"));
        context.sendMessage(Message.raw("%s".formatted(filePath)));
        context.sendMessage(Message.raw("You can edit this file to manually change spawn point data."));
        context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
    }
}
