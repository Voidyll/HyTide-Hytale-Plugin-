package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Displays help information for all HyTide commands.
 * Usage: /hytide-help [command]
 */
public class HytideHelpCommand extends CommandBase {
    private final OptionalArg<String> COMMAND_NAME;

    public HytideHelpCommand() {
        super("hytide-help", "Shows help for all HyTide commands");
        this.COMMAND_NAME = withOptionalArg("command", "Specific command to get detailed help for", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        String commandName = COMMAND_NAME.get(context);

        if (commandName != null && !commandName.isEmpty()) {
            showDetailedHelp(context, commandName.toLowerCase());
        } else {
            showOverview(context);
        }
    }

    private void showOverview(CommandContext context) {
        context.sendMessage(Message.raw("=== HyTide Command Reference ==="));
        context.sendMessage(Message.raw("Use /hytide-help <command> for detailed info"));
        context.sendMessage(Message.raw(""));

        context.sendMessage(Message.raw("--- Events ---"));
        context.sendMessage(Message.raw("/create-event - Create a new event"));
        context.sendMessage(Message.raw("/delete-event - Delete an event by ID"));
        context.sendMessage(Message.raw("/add-sub-event - Add a sub-event to an existing event"));
        context.sendMessage(Message.raw("/remove-sub-event - Remove a sub-event from an event"));
        context.sendMessage(Message.raw("/start-event - Manually start an event"));
        context.sendMessage(Message.raw("/stop-event - Stop the current event"));
        context.sendMessage(Message.raw("/list-events - List all events and status"));
        context.sendMessage(Message.raw(""));

        context.sendMessage(Message.raw("--- Spawns ---"));
        context.sendMessage(Message.raw("/create-spawn - Create a spawn marker at your location"));
        context.sendMessage(Message.raw("/delete-spawn - Delete a spawn marker by ID"));
        context.sendMessage(Message.raw("/list-spawns - List all spawn markers"));
        context.sendMessage(Message.raw("/create-item-spawn - Create an item spawn marker"));
        context.sendMessage(Message.raw("/delete-item-spawn - Delete an item spawn marker by ID"));
        context.sendMessage(Message.raw("/list-item-spawns - List all item spawn markers"));
        context.sendMessage(Message.raw("/trigger-spawn - Manually trigger spawns for a group"));
        context.sendMessage(Message.raw(""));

        context.sendMessage(Message.raw("--- Trigger Zones ---"));
        context.sendMessage(Message.raw("/create-trigger-zone - Create a trigger zone at your location"));
        context.sendMessage(Message.raw("/delete-trigger-zone - Delete a trigger zone by name"));
        context.sendMessage(Message.raw("/list-trigger-zones - List all trigger zones"));
        context.sendMessage(Message.raw(""));

        context.sendMessage(Message.raw("--- Spawn Director ---"));
        context.sendMessage(Message.raw("/horde-timer-pause - Pause horde timer"));
        context.sendMessage(Message.raw("/horde-timer-unpause - Unpause horde timer"));
        context.sendMessage(Message.raw("/horde-timer-restart - Restart horde timer"));
        context.sendMessage(Message.raw("/special-timer-pause - Pause special timer"));
        context.sendMessage(Message.raw("/special-timer-unpause - Unpause special timer"));
        context.sendMessage(Message.raw("/special-timer-restart - Restart special timer"));
        context.sendMessage(Message.raw(""));

        context.sendMessage(Message.raw("--- Data Files ---"));
        context.sendMessage(Message.raw("/show-spawn-markers-data-location - Show spawn markers file path"));
        context.sendMessage(Message.raw("/show-item-spawns-data-location - Show item spawns file path"));
        context.sendMessage(Message.raw("/show-trigger-zones-data-location - Show trigger zones file path"));
        context.sendMessage(Message.raw("/show-events-data-location - Show events file path"));
        context.sendMessage(Message.raw("/show-entity-roles-data-location - Show entity roles file path"));
        context.sendMessage(Message.raw(""));

        context.sendMessage(Message.raw("--- Utility ---"));
        context.sendMessage(Message.raw("/reset - Reset world (clean NPCs, respawn, reset events)"));
        context.sendMessage(Message.raw("/toggle-debug - Toggle debug output"));
        context.sendMessage(Message.raw("/hytide-help - This help command"));
    }

    private void showDetailedHelp(CommandContext context, String command) {
        switch (command) {
            case "create-event":
                context.sendMessage(Message.raw("=== /create-event ==="));
                context.sendMessage(Message.raw("Creates a new event with spawn director settings and end condition."));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Usage: /create-event <id> <endCondition> <hordeMinMs> <hordeMaxMs> <hordeWaveIntervalMs> <hordeWaveCount> <specialTimerMs> [endConditionValue] [triggerCoords] [repeatable]"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Required Args:"));
                context.sendMessage(Message.raw("  id - Unique event identifier"));
                context.sendMessage(Message.raw("  endCondition - How the event ends:"));
                context.sendMessage(Message.raw("    timer, entity_killed, trigger_zone, block_break, block_interaction"));
                context.sendMessage(Message.raw("  hordeMinMs - Min time between horde spawns (ms)"));
                context.sendMessage(Message.raw("  hordeMaxMs - Max time between horde spawns (ms)"));
                context.sendMessage(Message.raw("  hordeWaveIntervalMs - Time between waves in a horde (ms)"));
                context.sendMessage(Message.raw("  hordeWaveCount - Waves per horde cycle"));
                context.sendMessage(Message.raw("  specialTimerMs - Special spawn timer (ms)"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Optional Args:"));
                context.sendMessage(Message.raw("  endConditionValue - Depends on endCondition:"));
                context.sendMessage(Message.raw("    timer -> duration in ms (e.g. 180000)"));
                context.sendMessage(Message.raw("    entity_killed -> Role:count,Role:count (e.g. C_Shadow_Knight:2)"));
                context.sendMessage(Message.raw("    trigger_zone -> zone name (e.g. exit_zone)"));
                context.sendMessage(Message.raw("    block_break -> blockType:count (e.g. spawner:3)"));
                context.sendMessage(Message.raw("    block_interaction -> block type (e.g. lever_wall)"));
                context.sendMessage(Message.raw("  triggerCoords - Auto-start on block interact: x:y:z or x1:y1:z1;x2:y2:z2"));
                context.sendMessage(Message.raw("  repeatable - true/false (default: false)"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Example: /create-event wave1 timer 10000 15000 15000 5 20000 180000"));
                context.sendMessage(Message.raw("Example: /create-event boss1 entity_killed 20000 30000 25000 3 15000 C_Shadow_Knight:2 100:64:200"));
                break;

            case "delete-event":
                context.sendMessage(Message.raw("=== /delete-event ==="));
                context.sendMessage(Message.raw("Deletes an event by ID. Stops the event if it's running."));
                context.sendMessage(Message.raw("Usage: /delete-event <eventId>"));
                break;

            case "add-sub-event":
                context.sendMessage(Message.raw("=== /add-sub-event ==="));
                context.sendMessage(Message.raw("Adds a sub-event to an existing event. Sub-events fire actions when their trigger condition is met."));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Usage: /add-sub-event <eventId> <subEventId> <triggerType> <triggerData> <actionType> <actionData> [actionType2] [actionData2] [actionType3] [actionData3]"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("--- Trigger Types ---"));
                context.sendMessage(Message.raw("  timer - Fires after a delay (ms since event start)"));
                context.sendMessage(Message.raw("    triggerData: delayMs (e.g. 5000)"));
                context.sendMessage(Message.raw("  block_interaction - Fires when specified block types are interacted with"));
                context.sendMessage(Message.raw("    triggerData: blockType1;blockType2 (e.g. lever_wall;button_stone)"));
                context.sendMessage(Message.raw("  entity_killed - Fires after N total kills since event start"));
                context.sendMessage(Message.raw("    triggerData: count (e.g. 5)"));
                context.sendMessage(Message.raw("  sub_event_completion - Fires when another sub-event completes"));
                context.sendMessage(Message.raw("    triggerData: subEventId (e.g. phase1_done)"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("--- Action Types ---"));
                context.sendMessage(Message.raw("  add_blocks - Place blocks at coordinates"));
                context.sendMessage(Message.raw("    actionData: blockType@x1:y1:z1;x2:y2:z2"));
                context.sendMessage(Message.raw("  remove_blocks - Remove blocks (set to air)"));
                context.sendMessage(Message.raw("    actionData: x1:y1:z1;x2:y2:z2"));
                context.sendMessage(Message.raw("  spawn_npc - Spawn an NPC at a location"));
                context.sendMessage(Message.raw("    actionData: npcType@x:y:z (e.g. C_Shadow_Knight@100:64:200)"));
                context.sendMessage(Message.raw("  unlock_interaction - Unlock a block for interaction"));
                context.sendMessage(Message.raw("    actionData: blockType (e.g. lever_wall)"));
                context.sendMessage(Message.raw("  despawn_all_npcs - Remove all NPCs"));
                context.sendMessage(Message.raw("    actionData: none"));
                context.sendMessage(Message.raw("  execute_command - Run a server command"));
                context.sendMessage(Message.raw("    actionData: command with underscores for spaces (e.g. say_Hello_World)"));
                context.sendMessage(Message.raw("  set_block_state - Set a block's state (e.g. doors)"));
                context.sendMessage(Message.raw("    actionData: x:y:z@state (e.g. 100:64:200@OpenDoorIn)"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Example: /add-sub-event wave1 open_gate timer 10000 set_block_state 50:64:100@OpenDoorIn"));
                context.sendMessage(Message.raw("Example: /add-sub-event wave1 spawn_boss entity_killed 20 spawn_npc C_Shadow_Knight@100:64:200"));
                context.sendMessage(Message.raw("Example: /add-sub-event wave1 wall_down sub_event_completion open_gate remove_blocks 50:65:100;50:66:100;50:67:100"));
                break;

            case "remove-sub-event":
                context.sendMessage(Message.raw("=== /remove-sub-event ==="));
                context.sendMessage(Message.raw("Removes a sub-event from an existing event."));
                context.sendMessage(Message.raw("Usage: /remove-sub-event <eventId> <subEventId>"));
                context.sendMessage(Message.raw("Example: /remove-sub-event wave1 open_gate"));
                break;

            case "start-event":
                context.sendMessage(Message.raw("=== /start-event ==="));
                context.sendMessage(Message.raw("Manually starts an event. Stops any currently running event first."));
                context.sendMessage(Message.raw("Usage: /start-event <eventId>"));
                break;

            case "stop-event":
                context.sendMessage(Message.raw("=== /stop-event ==="));
                context.sendMessage(Message.raw("Stops the currently running event and restores default spawn rates."));
                context.sendMessage(Message.raw("Usage: /stop-event"));
                break;

            case "list-events":
                context.sendMessage(Message.raw("=== /list-events ==="));
                context.sendMessage(Message.raw("Lists all events and their current status (ongoing, started, not started)."));
                context.sendMessage(Message.raw("Usage: /list-events"));
                break;

            case "create-spawn":
                context.sendMessage(Message.raw("=== /create-spawn ==="));
                context.sendMessage(Message.raw("Creates a spawn marker at your current position."));
                context.sendMessage(Message.raw("Usage: /create-spawn <id> [entityType] <spawnNumber> <groupNumber> <role> [identifier] [pathName]"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Args:"));
                context.sendMessage(Message.raw("  id - Unique ID for this spawn marker"));
                context.sendMessage(Message.raw("  entityType - (Optional) Specific NPC type, or omit for random selection"));
                context.sendMessage(Message.raw("  spawnNumber - How many entities to spawn"));
                context.sendMessage(Message.raw("  groupNumber - Group number (used by trigger zones)"));
                context.sendMessage(Message.raw("  role - horde, ambient, boss, patrol, or special"));
                context.sendMessage(Message.raw("  identifier - (Optional) Link to a specific trigger"));
                context.sendMessage(Message.raw("  pathName - (Optional) Path name for patrol NPCs"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Example (horde): /create-spawn hall_rats 5 1 horde"));
                context.sendMessage(Message.raw("Example (boss): /create-spawn arena_boss HyTide_Shadow_Knight 1 3 boss boss_trigger"));
                context.sendMessage(Message.raw("Example (special): /create-spawn roof_sniper 2 1 special rooftop_id"));
                break;

            case "delete-spawn":
                context.sendMessage(Message.raw("=== /delete-spawn ==="));
                context.sendMessage(Message.raw("Deletes a spawn marker by its unique ID."));
                context.sendMessage(Message.raw("Usage: /delete-spawn <id>"));
                context.sendMessage(Message.raw("Use /list-spawns to see all spawn marker IDs."));
                context.sendMessage(Message.raw("Example: /delete-spawn hall_rats"));
                break;

            case "list-spawns":
                context.sendMessage(Message.raw("=== /list-spawns ==="));
                context.sendMessage(Message.raw("Lists all spawn markers with their ID, entity type, count, group, role, and position."));
                context.sendMessage(Message.raw("Usage: /list-spawns"));
                break;

            case "create-item-spawn":
                context.sendMessage(Message.raw("=== /create-item-spawn ==="));
                context.sendMessage(Message.raw("Creates an item spawn marker at your current position."));
                context.sendMessage(Message.raw("Items at these markers are respawned when /reset is used."));
                context.sendMessage(Message.raw("Usage: /create-item-spawn <id>"));
                context.sendMessage(Message.raw("Stand at the desired location and run the command with a unique ID."));
                context.sendMessage(Message.raw("Example: /create-item-spawn potion_alcove"));
                break;

            case "delete-item-spawn":
                context.sendMessage(Message.raw("=== /delete-item-spawn ==="));
                context.sendMessage(Message.raw("Deletes an item spawn marker by its unique ID."));
                context.sendMessage(Message.raw("Usage: /delete-item-spawn <id>"));
                context.sendMessage(Message.raw("Use /list-item-spawns to see all item spawn marker IDs."));
                context.sendMessage(Message.raw("Example: /delete-item-spawn potion_alcove"));
                break;

            case "list-item-spawns":
                context.sendMessage(Message.raw("=== /list-item-spawns ==="));
                context.sendMessage(Message.raw("Lists all item spawn markers with their ID, position, and world."));
                context.sendMessage(Message.raw("Usage: /list-item-spawns"));
                break;

            case "trigger-spawn":
                context.sendMessage(Message.raw("=== /trigger-spawn ==="));
                context.sendMessage(Message.raw("Manually triggers spawns for a specific group and role."));
                context.sendMessage(Message.raw("Usage: /trigger-spawn <groupNumber> <role>"));
                break;

            case "create-trigger-zone":
                context.sendMessage(Message.raw("=== /create-trigger-zone ==="));
                context.sendMessage(Message.raw("Creates a trigger zone at your position that activates spawns when entered."));
                context.sendMessage(Message.raw("Usage: /create-trigger-zone <name> <radius> <groupNumber> <type> [identifier]"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Args:"));
                context.sendMessage(Message.raw("  name - Unique zone name"));
                context.sendMessage(Message.raw("  radius - Detection radius"));
                context.sendMessage(Message.raw("  groupNumber - Spawn group to trigger"));
                context.sendMessage(Message.raw("  type - horde, ambient, boss, patrol, special"));
                context.sendMessage(Message.raw("  identifier - (Optional) Links to specific spawn markers with matching identifier"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Example: /create-trigger-zone hallway_entrance 15 1 horde"));
                context.sendMessage(Message.raw("Example: /create-trigger-zone boss_chamber 10 3 boss boss_trigger"));
                context.sendMessage(Message.raw("Example: /create-trigger-zone side_room 12 2 ambient"));
                break;

            case "delete-trigger-zone":
                context.sendMessage(Message.raw("=== /delete-trigger-zone ==="));
                context.sendMessage(Message.raw("Deletes a trigger zone by its unique name."));
                context.sendMessage(Message.raw("Usage: /delete-trigger-zone <name>"));
                context.sendMessage(Message.raw("Use /list-trigger-zones to see all trigger zone names."));
                context.sendMessage(Message.raw("Example: /delete-trigger-zone hallway_entrance"));
                break;

            case "list-trigger-zones":
                context.sendMessage(Message.raw("=== /list-trigger-zones ==="));
                context.sendMessage(Message.raw("Lists the information of all created trigger zones, including name, type, group, radius, and position."));
                context.sendMessage(Message.raw("Usage: /list-trigger-zones"));
                break;

            case "horde-timer-pause":
                context.sendMessage(Message.raw("=== /horde-timer-pause ==="));
                context.sendMessage(Message.raw("Pauses the timer that controls horde spawning."));
                context.sendMessage(Message.raw("Usage: /horde-timer-pause"));
                break;

            case "horde-timer-unpause":
                context.sendMessage(Message.raw("=== /horde-timer-unpause ==="));
                context.sendMessage(Message.raw("Unpauses the timer that controls horde spawning."));
                context.sendMessage(Message.raw("Usage: /horde-timer-unpause"));
                break;

            case "horde-timer-restart":
                context.sendMessage(Message.raw("=== /horde-timer-restart ==="));
                context.sendMessage(Message.raw("Restarts the timer that controls horde spawning."));
                context.sendMessage(Message.raw("Usage: /horde-timer-restart"));
                break;

            case "special-timer-pause":
                context.sendMessage(Message.raw("=== /special-timer-pause ==="));
                context.sendMessage(Message.raw("Pauses the timer that controls special spawning."));
                context.sendMessage(Message.raw("Usage: /special-timer-pause"));
                break;

            case "special-timer-unpause":
                context.sendMessage(Message.raw("=== /special-timer-unpause ==="));
                context.sendMessage(Message.raw("Unpauses the timer that controls special spawning."));
                context.sendMessage(Message.raw("Usage: /special-timer-unpause"));
                break;

            case "special-timer-restart":
                context.sendMessage(Message.raw("=== /special-timer-restart ==="));
                context.sendMessage(Message.raw("Restarts the timer that controls special spawning."));
                context.sendMessage(Message.raw("Usage: /special-timer-restart"));
                break;

            case "show-spawn-markers-data-location":
                context.sendMessage(Message.raw("=== /show-spawn-markers-data-location ==="));
                context.sendMessage(Message.raw("Shows the file path for the spawn_markers.json data file."));
                context.sendMessage(Message.raw("The data in this file can be edited to manually change spawn point properties without having to delete and recreate entries."));
                context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
                context.sendMessage(Message.raw("Usage: /show-spawn-markers-data-location"));
                break;

            case "show-item-spawns-data-location":
                context.sendMessage(Message.raw("=== /show-item-spawns-data-location ==="));
                context.sendMessage(Message.raw("Shows the file path for the item_spawns.json data file."));
                context.sendMessage(Message.raw("The data in this file can be edited to manually change item spawn point properties without having to delete and recreate entries."));
                context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
                context.sendMessage(Message.raw("Usage: /show-item-spawns-data-location"));
                break;

            case "show-trigger-zones-data-location":
                context.sendMessage(Message.raw("=== /show-trigger-zones-data-location ==="));
                context.sendMessage(Message.raw("Shows the file path for the trigger_zones.json data file."));
                context.sendMessage(Message.raw("The data in this file can be edited to manually change trigger zone properties without having to delete and recreate entries."));
                context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
                context.sendMessage(Message.raw("Usage: /show-trigger-zones-data-location"));
                break;

            case "show-events-data-location":
                context.sendMessage(Message.raw("=== /show-events-data-location ==="));
                context.sendMessage(Message.raw("Shows the file path for the events.json data file."));
                context.sendMessage(Message.raw("The data in this file can be edited to manually change event configurations without having to delete and recreate entries."));
                context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
                context.sendMessage(Message.raw("Usage: /show-events-data-location"));
                break;

            case "show-entity-roles-data-location":
                context.sendMessage(Message.raw("=== /show-entity-roles-data-location ==="));
                context.sendMessage(Message.raw("Shows the file path for the entity_roles.json data file."));
                context.sendMessage(Message.raw("This file controls which enemy types are included in each spawn role and their spawning weights."));
                context.sendMessage(Message.raw("Only edit this file if you want to add or remove enemy types from the spawn structure, or if you want to change the spawning weights of different enemies. Otherwise, it should be left alone."));
                context.sendMessage(Message.raw("Warning: Modifying the structure of an entry may prevent the system from using it correctly."));
                context.sendMessage(Message.raw("Usage: /show-entity-roles-data-location"));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Roles:"));
                context.sendMessage(Message.raw("  - horde: Entities in this role will be used to spawn from horde spawn points. Horde spawns are spawned in waves on a timer that the spawn directer controls. Horde spawns will be the most common type of enemy encounter, and the balance of weak and strong enemies in a horde is dependent on the challenge you desire."));
                context.sendMessage(Message.raw("  - ambient: Entities in this role will be used to spawn from ambient spawn points. When a player enters a checkpoint trigger zone, all ambient spawn points that share a group number with that trigger zone will be triggered. Ambient enemies will be comprised of many of the same enemies as are in hordes, and mainly server the purpose of smoothing out the pacing of the map and to reduce the downtime between enemy spawns."));
                context.sendMessage(Message.raw("  - patrol: Entities in this role will be used to spawn from patrol spawn points. Patrol spawn points have a chance to be triggered when a player enters a patrol trigger zone. You can link a path to the patrol spawn point so the patrol enemies will follow that set path. Only one boss or patrol will be spawned per group number. Patrols are meant to be difficult yet avoidable, and are filled with exclusively the strongest of your horde and ambient units."));
                context.sendMessage(Message.raw("  - boss: Entities in this role will be used to spawn from boss spawn points. Boss spawn points have a chance to be triggered when a player enters a boss trigger zone. Only one boss or patrol will be spawned per group number. Bosses are the the strongest single units in your spawn structure and are only seen as boss spawns."));
                context.sendMessage(Message.raw("  - special: Entities in this role will be used to spawn from special spawn points. Special spawns are spawned on a timer that the spawn director controls, and is separate from the horde spawn timer. Special enemies are units that add complexity and diversity to the spawn structure, they are spawned in low numbers but individually are threatening."));
                context.sendMessage(Message.raw(""));
                context.sendMessage(Message.raw("Fields:"));
                context.sendMessage(Message.raw("  - entityName: The internal name of the enemy type (e.g. HyTide_Shadow_Knight)"));
                context.sendMessage(Message.raw("  - cap: The maximum number of this enemy that can spawn in one spawn wave."));
                context.sendMessage(Message.raw("  - weight: The relative spawning weight of this enemy compared to other enemies in the same role. A higher weight means the enemy is more likely to spawn. Two enemies having the same weight means they are equally likely to spawn."));
                break;

            case "toggle-debug":
                context.sendMessage(Message.raw("=== /toggle-debug ==="));
                context.sendMessage(Message.raw("Toggles the presence of various debug logs that appear in the game chat."));
                context.sendMessage(Message.raw("Debug logs include: horde and special timer countdowns, block interaction events, event status updates, entity cleanup system logs, and more."));
                context.sendMessage(Message.raw("Usage: /toggle-debug"));
                break;

            case "reset":
                context.sendMessage(Message.raw("=== /reset ==="));
                context.sendMessage(Message.raw("Resets the entire world state:"));
                context.sendMessage(Message.raw("  - Cleans all NPCs"));
                context.sendMessage(Message.raw("  - Resets door states"));
                context.sendMessage(Message.raw("  - Teleports players to spawn"));
                context.sendMessage(Message.raw("  - Clears active groups and triggers"));
                context.sendMessage(Message.raw("  - Stops running events"));
                context.sendMessage(Message.raw("  - Respawns items at markers"));
                context.sendMessage(Message.raw("  - Randomizes boss/patrol assignments"));
                context.sendMessage(Message.raw("Usage: /reset"));
                break;

            default:
                context.sendMessage(Message.raw("Unknown command: " + command));
                context.sendMessage(Message.raw("Use /hytide-help to see all commands"));
                break;
        }
    }
}
