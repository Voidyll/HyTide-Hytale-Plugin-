package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.TriggerZoneManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DeleteTriggerZoneCommand extends CommandBase {
    private final RequiredArg<String> ZONE_NAME;
    private final TriggerZoneManager zoneManager;

    public DeleteTriggerZoneCommand(TriggerZoneManager zoneManager) {
        super("delete-trigger-zone", "Deletes a trigger zone by name");
        this.zoneManager = zoneManager;
        this.ZONE_NAME = withRequiredArg("name", "The name of the trigger zone to delete", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String zoneName = ZONE_NAME.get(context);

        if (zoneManager.removeZoneByName(zoneName)) {
            context.sendMessage(Message.raw("Trigger zone '" + zoneName + "' deleted."));
        } else {
            context.sendMessage(Message.raw("Trigger zone '" + zoneName + "' not found."));
        }
    }
}
