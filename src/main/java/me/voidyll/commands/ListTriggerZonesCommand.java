package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.data.TriggerZoneData;
import me.voidyll.data.TriggerZoneManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;

public class ListTriggerZonesCommand extends CommandBase {
    private final TriggerZoneManager zoneManager;

    public ListTriggerZonesCommand(TriggerZoneManager zoneManager) {
        super("list-trigger-zones", "Lists all configured trigger zones");
        this.zoneManager = zoneManager;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        List<TriggerZoneData> zones = zoneManager.loadZones();

        if (zones.isEmpty()) {
            context.sendMessage(Message.raw("No trigger zones defined."));
            return;
        }

        context.sendMessage(Message.raw("Trigger Zones:"));
        for (TriggerZoneData zone : zones) {
            String type = zone.getType() != null ? zone.getType() : "checkpoint";
            context.sendMessage(Message.raw(
                "- %s | type=%s | group %d | r=%.2f | (%.1f, %.1f, %.1f) | world=%s"
                    .formatted(zone.getName(), type, zone.getGroupNumber(), zone.getRadius(),
                              zone.getX(), zone.getY(), zone.getZ(), zone.getWorldName())
            ));
        }
    }
}
