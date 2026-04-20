package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.SpawnDataManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OpenSpawnsCommand extends CommandBase {
    private final SpawnDataManager dataManager;

    public OpenSpawnsCommand(SpawnDataManager dataManager) {
        super("open-spawns", "Displays the file path to the spawn markers data file");
        this.dataManager = dataManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String filePath = dataManager.getDataFilePath().toAbsolutePath().toString();
        
        context.sendMessage(Message.raw("Spawn markers data file location:"));
        context.sendMessage(Message.raw("%s".formatted(filePath)));
        context.sendMessage(Message.raw("You can open this file in VS Code or any text editor to manually edit spawn points."));
    }
}
