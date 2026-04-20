package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.TriggerZoneManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ShowTriggerZonesDataLocationCommand extends CommandBase {
    private final TriggerZoneManager zoneManager;

    public ShowTriggerZonesDataLocationCommand(TriggerZoneManager zoneManager) {
        super("show-trigger-zones-data-location", "Shows the file path for trigger zones data");
        this.zoneManager = zoneManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String filePath = zoneManager.getDataFilePath().toAbsolutePath().toString();
        
        context.sendMessage(Message.raw("Trigger zones data file location:"));
        context.sendMessage(Message.raw("%s".formatted(filePath)));
        context.sendMessage(Message.raw("You can edit this file to manually change trigger zone data."));
        context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
    }
}
