package me.voidyll.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.EventHandler;
import me.voidyll.systems.events.SubEvent;
import me.voidyll.systems.events.SubEventAction;
import me.voidyll.systems.events.SubEventTrigger;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to add a sub-event to an existing event.
 */
public class AddSubEventCommand extends CommandBase {
    private final EventHandler eventHandler;

    private final RequiredArg<String> EVENT_ID;
    private final RequiredArg<String> SUB_EVENT_ID;
    private final RequiredArg<String> TRIGGER_TYPE;
    private final RequiredArg<String> TRIGGER_DATA;
    private final RequiredArg<String> ACTION_TYPE;
    private final RequiredArg<String> ACTION_DATA;
    private final OptionalArg<String> ACTION_TYPE_2;
    private final OptionalArg<String> ACTION_DATA_2;
    private final OptionalArg<String> ACTION_TYPE_3;
    private final OptionalArg<String> ACTION_DATA_3;

    public AddSubEventCommand(EventHandler eventHandler) {
        super("add-sub-event", "Add a sub-event to an existing event");
        this.eventHandler = eventHandler;

        this.EVENT_ID = withRequiredArg("eventId", "ID of the parent event", ArgTypes.STRING);
        this.SUB_EVENT_ID = withRequiredArg("subEventId", "Unique ID for this sub-event", ArgTypes.STRING);
        this.TRIGGER_TYPE = withRequiredArg("triggerType", "Trigger type: timer, block_interaction, entity_killed, sub_event_completion", ArgTypes.STRING);
        this.TRIGGER_DATA = withRequiredArg("triggerData", "Trigger data (format depends on trigger type)", ArgTypes.STRING);
        this.ACTION_TYPE = withRequiredArg("actionType", "Action type: add_blocks, remove_blocks, spawn_npc, unlock_interaction, despawn_all_npcs, execute_command, set_block_state", ArgTypes.STRING);
        this.ACTION_DATA = withRequiredArg("actionData", "Action data (format depends on action type)", ArgTypes.STRING);
        this.ACTION_TYPE_2 = withOptionalArg("actionType2", "Second action type (optional)", ArgTypes.STRING);
        this.ACTION_DATA_2 = withOptionalArg("actionData2", "Second action data (optional)", ArgTypes.STRING);
        this.ACTION_TYPE_3 = withOptionalArg("actionType3", "Third action type (optional)", ArgTypes.STRING);
        this.ACTION_DATA_3 = withOptionalArg("actionData3", "Third action data (optional)", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String eventId = EVENT_ID.get(context);
        String subEventId = SUB_EVENT_ID.get(context);
        String triggerTypeStr = TRIGGER_TYPE.get(context);
        String triggerDataStr = TRIGGER_DATA.get(context);
        String actionTypeStr = ACTION_TYPE.get(context);
        String actionDataStr = ACTION_DATA.get(context);
        String actionType2Str = ACTION_TYPE_2.get(context);
        String actionData2Str = ACTION_DATA_2.get(context);
        String actionType3Str = ACTION_TYPE_3.get(context);
        String actionData3Str = ACTION_DATA_3.get(context);

        // Parse trigger
        SubEventTrigger trigger = parseTrigger(triggerTypeStr, triggerDataStr, context);
        if (trigger == null) return;

        // Parse actions
        List<SubEventAction> actions = new ArrayList<>();

        SubEventAction action1 = parseAction(actionTypeStr, actionDataStr, context);
        if (action1 == null) return;
        actions.add(action1);

        if (actionType2Str != null && !actionType2Str.isEmpty()
                && actionData2Str != null && !actionData2Str.isEmpty()) {
            SubEventAction action2 = parseAction(actionType2Str, actionData2Str, context);
            if (action2 == null) return;
            actions.add(action2);
        }

        if (actionType3Str != null && !actionType3Str.isEmpty()
                && actionData3Str != null && !actionData3Str.isEmpty()) {
            SubEventAction action3 = parseAction(actionType3Str, actionData3Str, context);
            if (action3 == null) return;
            actions.add(action3);
        }

        // Build sub-event
        SubEvent subEvent = new SubEvent(subEventId, trigger, actions);
        subEvent.parseTypes();

        boolean success = eventHandler.addSubEvent(eventId, subEvent);
        if (success) {
            context.sendMessage(Message.raw("Sub-event '" + subEventId + "' added to event '" + eventId + "'"));
            context.sendMessage(Message.raw("  Trigger: " + triggerTypeStr));
            context.sendMessage(Message.raw("  Actions: " + actions.size()));
        } else {
            context.sendMessage(Message.raw("Event '" + eventId + "' not found."));
        }
    }

    private SubEventTrigger parseTrigger(String typeStr, String dataStr, CommandContext context) {
        String type = typeStr.toUpperCase();
        JsonObject triggerData = new JsonObject();

        switch (type) {
            case "TIMER":
                // triggerData format: delayMs (e.g. "5000")
                try {
                    long delayMs = Long.parseLong(dataStr);
                    triggerData.addProperty("delayMs", delayMs);
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid timer delay: '" + dataStr + "'. Must be a number (ms)."));
                    return null;
                }
                break;

            case "BLOCK_INTERACTION":
                // triggerData format: blockType1;blockType2 (e.g. "lever_wall;button_stone")
                JsonArray blockArray = new JsonArray();
                for (String bt : dataStr.split(";")) {
                    blockArray.add(bt.trim());
                }
                triggerData.add("blockTypes", blockArray);
                break;

            case "ENTITY_KILLED":
                // triggerData format: count (e.g. "5")
                try {
                    int count = Integer.parseInt(dataStr);
                    triggerData.addProperty("count", count);
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid kill count: '" + dataStr + "'. Must be a number."));
                    return null;
                }
                break;

            case "SUB_EVENT_COMPLETION":
                // triggerData format: subEventId (e.g. "phase1_complete")
                triggerData.addProperty("subEventId", dataStr);
                break;

            default:
                context.sendMessage(Message.raw("Unknown trigger type: '" + typeStr + "'"));
                context.sendMessage(Message.raw("Valid types: timer, block_interaction, entity_killed, sub_event_completion"));
                return null;
        }

        return new SubEventTrigger(type, triggerData);
    }

    private SubEventAction parseAction(String typeStr, String dataStr, CommandContext context) {
        String type = typeStr.toUpperCase();
        JsonObject actionData = new JsonObject();

        switch (type) {
            case "ADD_BLOCKS":
                // format: blockType@x1:y1:z1;x2:y2:z2 (e.g. "stone@100:64:200;101:64:200")
                return parseBlockAction(type, dataStr, true, context);

            case "REMOVE_BLOCKS":
                // format: x1:y1:z1;x2:y2:z2 (e.g. "100:64:200;101:64:200")
                return parseBlockAction(type, dataStr, false, context);

            case "SPAWN_NPC":
                // format: npcType@x:y:z (e.g. "HyTide_Shadow_Knight@100:64:200")
                if (!dataStr.contains("@")) {
                    context.sendMessage(Message.raw("SPAWN_NPC format: npcType@x:y:z (e.g. C_Shadow_Knight@100:64:200)"));
                    return null;
                }
                String[] spawnParts = dataStr.split("@", 2);
                String npcType = spawnParts[0].trim();
                String[] spawnCoords = spawnParts[1].trim().split(":");
                if (spawnCoords.length != 3) {
                    context.sendMessage(Message.raw("Invalid SPAWN_NPC coords. Use npcType@x:y:z"));
                    return null;
                }
                try {
                    actionData.addProperty("npcType", npcType);
                    actionData.addProperty("x", Integer.parseInt(spawnCoords[0].trim()));
                    actionData.addProperty("y", Integer.parseInt(spawnCoords[1].trim()));
                    actionData.addProperty("z", Integer.parseInt(spawnCoords[2].trim()));
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid SPAWN_NPC coordinate numbers."));
                    return null;
                }
                break;

            case "UNLOCK_INTERACTION":
                // format: blockType (e.g. "lever_wall")
                actionData.addProperty("blockType", dataStr);
                break;

            case "DESPAWN_ALL_NPCS":
                // format: ignored (pass "none" or any value)
                break;

            case "EXECUTE_COMMAND":
                // format: command string (e.g. "say Hello World")
                // Replace underscores with spaces so multi-word commands work
                actionData.addProperty("command", dataStr.replace('_', ' '));
                break;

            case "SET_BLOCK_STATE":
                // format: x:y:z@state (e.g. "100:64:200@OpenDoorIn")
                if (!dataStr.contains("@")) {
                    context.sendMessage(Message.raw("SET_BLOCK_STATE format: x:y:z@state (e.g. 100:64:200@OpenDoorIn)"));
                    return null;
                }
                String[] stateParts = dataStr.split("@", 2);
                String[] stateCoords = stateParts[0].trim().split(":");
                if (stateCoords.length != 3) {
                    context.sendMessage(Message.raw("Invalid SET_BLOCK_STATE coords. Use x:y:z@state"));
                    return null;
                }
                try {
                    actionData.addProperty("x", Integer.parseInt(stateCoords[0].trim()));
                    actionData.addProperty("y", Integer.parseInt(stateCoords[1].trim()));
                    actionData.addProperty("z", Integer.parseInt(stateCoords[2].trim()));
                    actionData.addProperty("state", stateParts[1].trim());
                } catch (NumberFormatException e) {
                    context.sendMessage(Message.raw("Invalid SET_BLOCK_STATE coordinate numbers."));
                    return null;
                }
                break;

            default:
                context.sendMessage(Message.raw("Unknown action type: '" + typeStr + "'"));
                context.sendMessage(Message.raw("Valid types: add_blocks, remove_blocks, spawn_npc, unlock_interaction, despawn_all_npcs, execute_command, set_block_state"));
                return null;
        }

        return new SubEventAction(type, actionData);
    }

    private SubEventAction parseBlockAction(String type, String dataStr, boolean requiresBlockType, CommandContext context) {
        JsonObject actionData = new JsonObject();

        String coordsStr;
        if (requiresBlockType) {
            // ADD_BLOCKS: blockType@x1:y1:z1;x2:y2:z2
            if (!dataStr.contains("@")) {
                context.sendMessage(Message.raw("ADD_BLOCKS format: blockType@x1:y1:z1;x2:y2:z2"));
                return null;
            }
            String[] parts = dataStr.split("@", 2);
            actionData.addProperty("blockType", parts[0].trim());
            coordsStr = parts[1].trim();
        } else {
            // REMOVE_BLOCKS: x1:y1:z1;x2:y2:z2
            coordsStr = dataStr;
        }

        JsonArray coordsArray = new JsonArray();
        for (String group : coordsStr.split(";")) {
            String[] xyz = group.trim().split(":");
            if (xyz.length != 3) {
                context.sendMessage(Message.raw("Invalid coordinate: '" + group + "'. Use x:y:z"));
                return null;
            }
            try {
                JsonArray coord = new JsonArray();
                coord.add(Integer.parseInt(xyz[0].trim()));
                coord.add(Integer.parseInt(xyz[1].trim()));
                coord.add(Integer.parseInt(xyz[2].trim()));
                coordsArray.add(coord);
            } catch (NumberFormatException e) {
                context.sendMessage(Message.raw("Invalid coordinate numbers in: '" + group + "'"));
                return null;
            }
        }

        actionData.add("coordinates", coordsArray);
        return new SubEventAction(type, actionData);
    }
}
