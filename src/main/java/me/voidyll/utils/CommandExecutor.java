package me.voidyll.utils;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for executing Hytale commands programmatically.
 * 
 * This class provides convenient methods to execute commands as console,
 * which has all permissions needed for operations like NPC spawning.
 */
public class CommandExecutor {

    /**
     * Execute a single command as console.
     * 
     * @param command The command string to execute (e.g., "npc spawn Rat")
     * @return CompletableFuture that completes when the command finishes
     */
    public static CompletableFuture<Void> executeCommand(String command) {
        CommandManager cmdManager = HytaleServer.get().getCommandManager();
        return cmdManager.handleCommand(ConsoleSender.INSTANCE, command);
    }

    /**
     * Execute a command as a specific sender (allows player context).
     * 
     * @param sender The command sender (console, player, etc.)
     * @param command The command string to execute
     * @return CompletableFuture that completes when the command finishes
     */
    public static CompletableFuture<Void> executeCommand(CommandSender sender, String command) {
        CommandManager cmdManager = HytaleServer.get().getCommandManager();
        return cmdManager.handleCommand(sender, command);
    }

    /**
     * Execute a single command and wait for completion (blocking).
     * 
     * @param command The command string to execute
     * @throws Exception if the command fails or is interrupted
     */
    public static void executeCommandSync(String command) throws Exception {
        executeCommand(command).join();
    }

    /**
     * Execute a command as a specific sender and wait for completion (blocking).
     * 
     * @param sender The command sender (console, player, etc.)
     * @param command The command string to execute
     * @throws Exception if the command fails or is interrupted
     */
    public static void executeCommandSync(CommandSender sender, String command) throws Exception {
        executeCommand(sender, command).join();
    }

    /**
     * Execute multiple commands sequentially, waiting for each to complete.
     * 
     * @param commands List of command strings to execute in order
     * @param context CommandContext to send feedback messages (optional, can be null)
     * @return Total number of successfully executed commands
     */
    public static int executeCommandsSequential(List<String> commands, CommandContext context) {
        int successCount = 0;
        
        for (String command : commands) {
            try {
                executeCommandSync(command);
                successCount++;
                
                if (context != null) {
                    context.sendMessage(Message.raw("✓ " + command));
                }
            } catch (Exception e) {
                if (context != null) {
                    context.sendMessage(Message.raw(
                        String.format("✗ Failed: %s - %s", command, e.getMessage())
                    ));
                }
            }
        }
        
        return successCount;
    }

    /**
     * Execute multiple commands in parallel (non-blocking).
     * 
     * @param commands List of command strings to execute
     * @return CompletableFuture that completes when all commands finish
     */
    public static CompletableFuture<Void> executeCommandsParallel(List<String> commands) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String command : commands) {
            futures.add(executeCommand(command));
        }
        
        // Wait for all futures to complete
        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
    }

    /**
     * Spawn multiple entities with the given entity type.
     * 
     * @param entityType The entity/role name to spawn (e.g., "Rat", "Goblin")
     * @param count Number of entities to spawn
     * @param context CommandContext for feedback messages
     * @return Number of successfully spawned entities
     */
    public static int spawnMultipleEntities(String entityType, int count, CommandContext context) {
        int successCount = 0;
        
        for (int i = 0; i < count; i++) {
            try {
                String command = String.format("npc spawn %s", entityType);
                executeCommandSync(command);
                successCount++;
                
                if (context != null) {
                    context.sendMessage(Message.raw(
                        String.format("[%d/%d] Spawned %s", i + 1, count, entityType)
                    ));
                }
            } catch (Exception e) {
                if (context != null) {
                    context.sendMessage(Message.raw(
                        String.format("Failed to spawn %s: %s", entityType, e.getMessage())
                    ));
                }
            }
        }
        
        return successCount;
    }

    /**
     * Execute commands on a specific world's thread (for world-dependent operations).
     * 
     * @param world The world to execute commands on
     * @param commands List of commands to execute
     * @param context CommandContext for feedback (optional)
     * @return Number of successfully executed commands
     */
    public static int executeCommandsOnWorldThread(World world, List<String> commands, CommandContext context) {
        final int[] successCount = {0};
        
        world.execute(() -> {
            for (String command : commands) {
                try {
                    executeCommandSync(command);
                    successCount[0]++;
                    
                    if (context != null) {
                        context.sendMessage(Message.raw("✓ " + command));
                    }
                } catch (Exception e) {
                    if (context != null) {
                        context.sendMessage(Message.raw(
                            String.format("✗ Failed: %s", command)
                        ));
                    }
                }
            }
        });
        
        return successCount[0];
    }

    /**
     * Execute a command and chain additional operations.
     * 
     * Example:
     *   executeCommand("npc spawn Rat")
     *       .thenRun(() -> System.out.println("Rat spawned!"))
     *       .thenRun(() -> System.out.println("Ready for next command"));
     * 
     * @param command The command to execute
     * @return CompletableFuture for chaining operations
     */
    public static CompletableFuture<Void> executeCommandWithChain(String command) {
        return executeCommand(command);
    }
}
