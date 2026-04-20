package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.RoleConfigManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ShowEntityRolesDataLocationCommand extends CommandBase {
    private final RoleConfigManager roleConfigManager;

    public ShowEntityRolesDataLocationCommand(RoleConfigManager roleConfigManager) {
        super("show-entity-roles-data-location", "Shows the file path for entity roles data");
        this.roleConfigManager = roleConfigManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String filePath = roleConfigManager.getConfigFilePath().toAbsolutePath().toString();
        
        context.sendMessage(Message.raw("Entity roles data file location:"));
        context.sendMessage(Message.raw("%s".formatted(filePath)));
        context.sendMessage(Message.raw("This file controls which enemy types are included in each spawn role and their spawning weights."));
        context.sendMessage(Message.raw("Only edit this file if you want to add/remove enemy types from the spawn structure or change their weights."));
        context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
    }
}
