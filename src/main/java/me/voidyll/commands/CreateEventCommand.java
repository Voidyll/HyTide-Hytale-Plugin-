package me.voidyll.commands;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import me.voidyll.systems.events.EventConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to create a new event with all required fields.
 *
 * End condition types: timer, entity_killed, trigger_zone, block_break, block_interaction
 * 
 * endConditionValue depends on endCondition type:
 *   timer           -> duration in ms (e.g. "180000")
 *   entity_killed   -> "EntityRole:count,EntityRole:count" (e.g. "HyTide_Shadow_Knight:2,C_Rex_Cave:1")
 *   trigger_zone    -> zone name (e.g. "exit_zone")
 *   block_break     -> "blockType:count" (e.g. "spawner:3")
 *   block_interaction -> block type (e.g. "lever_wall")
 * 
 * triggerCoords (optional): Coordinates that auto-start this event on block interaction.
 *   Format: "x,y,z" or "x1,y1,z1;x2,y2,z2" for multiple
 * 
 * repeatable (optional): "true" or "false" (default: false)
 */
public class CreateEventCommand extends CommandBase {
    private final RequiredArg<String> EVENT_ID;
    private final RequiredArg<String> END_CONDITION;
    private final RequiredArg<Integer> HORDE_MIN_MS;
    private final RequiredArg<Integer> HORDE_MAX_MS;
    private final RequiredArg<Integer> HORDE_WAVE_INTERVAL_MS;
    private final RequiredArg<Integer> HORDE_WAVE_COUNT;
    private final RequiredArg<Integer> SPECIAL_TIMER_MS;
    private final OptionalArg<String> END_CONDITION_VALUE;
    private final OptionalArg<String> TRIGGER_COORDS;
    private final OptionalArg<String> REPEATABLE;

    private final EventHandler eventHandler;

    public CreateEventCommand(EventHandler eventHandler) {
        super("create-event", "Creates a new event with specified parameters");
        this.eventHandler = eventHandler;

        this.EVENT_ID = withRequiredArg("eventId", "Unique ID for this event", ArgTypes.STRING);
        this.END_CONDITION = withRequiredArg("endCondition", "End condition type: timer, entity_killed, trigger_zone, block_break, block_interaction", ArgTypes.STRING);
        this.HORDE_MIN_MS = withRequiredArg("hordeMinMs", "Minimum horde timer in ms", ArgTypes.INTEGER);
        this.HORDE_MAX_MS = withRequiredArg("hordeMaxMs", "Maximum horde timer in ms", ArgTypes.INTEGER);
        this.HORDE_WAVE_INTERVAL_MS = withRequiredArg("hordeWaveIntervalMs", "Time between horde waves in ms", ArgTypes.INTEGER);
        this.HORDE_WAVE_COUNT = withRequiredArg("hordeWaveCount", "Number of waves per horde", ArgTypes.INTEGER);
        this.SPECIAL_TIMER_MS = withRequiredArg("specialTimerMs", "Special spawn timer in ms", ArgTypes.INTEGER);
        this.END_CONDITION_VALUE = withOptionalArg("endConditionValue", "Value for end condition (see /hytide-help for format)", ArgTypes.STRING);
        this.TRIGGER_COORDS = withOptionalArg("triggerCoords", "Auto-trigger coordinates: x:y:z or x1:y1:z1;x2:y2:z2", ArgTypes.STRING);
        this.REPEATABLE = withOptionalArg("repeatable", "Whether event can trigger multiple times (true/false)", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String eventId = EVENT_ID.get(context);
        String endCondition = END_CONDITION.get(context);
        int hordeMinMs = HORDE_MIN_MS.get(context);
        int hordeMaxMs = HORDE_MAX_MS.get(context);
        int hordeWaveIntervalMs = HORDE_WAVE_INTERVAL_MS.get(context);
        int hordeWaveCount = HORDE_WAVE_COUNT.get(context);
        int specialTimerMs = SPECIAL_TIMER_MS.get(context);
        String endConditionValue = END_CONDITION_VALUE.get(context);
        String triggerCoords = TRIGGER_COORDS.get(context);
        String repeatable = REPEATABLE.get(context);

        // Validate end condition type
        String normalizedEndCondition = endCondition.toLowerCase();
        if (!isValidEndCondition(normalizedEndCondition)) {
            context.sendMessage(Message.raw("Invalid end condition: " + endCondition));
            context.sendMessage(Message.raw("Valid types: timer, entity_killed, trigger_zone, block_break, block_interaction"));
            return;
        }

        // Validate timer values
        if (hordeMinMs < 0 || hordeMaxMs < 0 || hordeWaveIntervalMs < 0 || hordeWaveCount < 0 || specialTimerMs < 0) {
            context.sendMessage(Message.raw("All timer/count values must be non-negative"));
            return;
        }

        if (hordeMinMs > hordeMaxMs) {
            context.sendMessage(Message.raw("hordeMinMs cannot be greater than hordeMaxMs"));
            return;
        }

        // Build EventConfig
        EventConfig config = new EventConfig();
        config.setEventId(eventId);
        config.setHordeTimerMinMs(hordeMinMs);
        config.setHordeTimerMaxMs(hordeMaxMs);
        config.setHordeWaveIntervalMs(hordeWaveIntervalMs);
        config.setHordeWaveCount(hordeWaveCount);
        config.setSpecialTimerMs(specialTimerMs);
        config.setEndConditionType(normalizedEndCondition);
        config.setRepeatable("true".equalsIgnoreCase(repeatable));

        // Parse end condition data
        JsonObject endConditionData = parseEndConditionData(normalizedEndCondition, endConditionValue, context);
        if (endConditionData == null && requiresEndConditionValue(normalizedEndCondition)) {
            // Error already sent in parseEndConditionData
            return;
        }
        config.setEndConditionData(endConditionData);

        // Parse trigger coordinates
        if (triggerCoords != null && !triggerCoords.isEmpty()) {
            List<int[]> coords = parseTriggerCoordinates(triggerCoords, context);
            if (coords == null) {
                // Error already sent
                return;
            }
            config.setTriggerBlockCoordinates(coords);
        }

        // Add event
        if (eventHandler.addEvent(config)) {
            context.sendMessage(Message.raw("Event '" + eventId + "' created successfully!"));
            context.sendMessage(Message.raw("  End condition: " + normalizedEndCondition));
            if (triggerCoords != null && !triggerCoords.isEmpty()) {
                context.sendMessage(Message.raw("  Auto-trigger at: " + triggerCoords));
            }
            context.sendMessage(Message.raw("  Use /start-event " + eventId + " to start manually"));
        } else {
            context.sendMessage(Message.raw("Event '" + eventId + "' already exists. Use /delete-event to remove it first."));
        }
    }

    private boolean isValidEndCondition(String type) {
        return type.equals("timer") || type.equals("entity_killed") || type.equals("trigger_zone")
            || type.equals("block_break") || type.equals("block_interaction");
    }

    private boolean requiresEndConditionValue(String type) {
        // All end condition types require a value
        return true;
    }

    private JsonObject parseEndConditionData(String type, String value, CommandContext context) {
        if (value == null || value.isEmpty()) {
            if (type.equals("timer")) {
                // Default 3 minutes if no value provided
                JsonObject data = new JsonObject();
                data.addProperty("durationMs", 180000);
                return data;
            }
            context.sendMessage(Message.raw("End condition '" + type + "' requires endConditionValue"));
            context.sendMessage(Message.raw("Use /hytide-help for format details"));
            return null;
        }

        JsonObject data = new JsonObject();

        switch (type) {
            case "timer":
                try {
                    long durationMs = Long.parseLong(value);
                    data.addProperty("durationMs", durationMs);
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Timer duration must be a number in ms (e.g. 180000)"));
                    return null;
                }
                break;

            case "entity_killed":
                // Format: "EntityRole:count,EntityRole:count"
                JsonObject entities = new JsonObject();
                String[] entityPairs = value.split(",");
                for (String pair : entityPairs) {
                    String[] parts = pair.split(":");
                    if (parts.length != 2) {
                        context.sendMessage(Message.raw("Invalid entity_killed format. Use: EntityRole:count,EntityRole:count"));
                        return null;
                    }
                    try {
                        entities.addProperty(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    } catch (NumberFormatException e) {
                        context.sendMessage(Message.raw("Invalid kill count for '" + parts[0] + "': " + parts[1]));
                        return null;
                    }
                }
                data.add("entities", entities);
                break;

            case "trigger_zone":
                data.addProperty("zoneName", value);
                break;

            case "block_break":
                // Format: "blockType:count"
                String[] breakParts = value.split(":");
                if (breakParts.length != 2) {
                    context.sendMessage(Message.raw("Invalid block_break format. Use: blockType:count"));
                    return null;
                }
                data.addProperty("blockType", breakParts[0].trim());
                try {
                    data.addProperty("count", Integer.parseInt(breakParts[1].trim()));
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid block break count: " + breakParts[1]));
                    return null;
                }
                break;

            case "block_interaction":
                data.addProperty("blockType", value);
                break;

            default:
                context.sendMessage(Message.raw("Unknown end condition type: " + type));
                return null;
        }

        return data;
    }

    private List<int[]> parseTriggerCoordinates(String coordsStr, CommandContext context) {
        List<int[]> result = new ArrayList<>();
        String[] coordGroups = coordsStr.split(";");

        for (String group : coordGroups) {
            String[] parts = group.trim().split(":");
            if (parts.length != 3) {
                context.sendMessage(Message.raw("Invalid coordinate format: '" + group + "'. Use x:y:z"));
                return null;
            }
            try {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());
                result.add(new int[]{x, y, z});
            } catch (NumberFormatException e) {
                context.sendMessage(Message.raw("Invalid coordinate numbers in: '" + group + "'"));
                return null;
            }
        }

        return result;
    }
}
