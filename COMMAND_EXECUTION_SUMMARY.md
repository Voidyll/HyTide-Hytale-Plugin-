# Hytale Command Execution - Complete Implementation Guide

## Quick Answer

### To execute a command programmatically in Hytale:

```java
// 1. Get the CommandManager
CommandManager cmdManager = HytaleServer.get().getCommandManager();

// 2. Get a CommandSender (ConsoleSender has all permissions)
CommandSender consoleSender = ConsoleSender.INSTANCE;

// 3. Execute the command
CompletableFuture<Void> future = cmdManager.handleCommand(consoleSender, "npc spawn Rat");

// 4. Wait for completion (or use async)
future.join();
```

---

## Method Signature

```java
// From CommandManager class
public CompletableFuture<Void> handleCommand(CommandSender sender, String command)
public CompletableFuture<Void> handleCommand(PlayerRef playerRef, String command)
```

---

## Complete Code Example: Spawn 5 Rats

```java
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public void spawnRats(CommandContext context) {
    CommandManager cmdManager = HytaleServer.get().getCommandManager();
    ConsoleSender consoleSender = ConsoleSender.INSTANCE;
    
    int successCount = 0;
    for (int i = 0; i < 5; i++) {
        try {
            CompletableFuture<Void> future = cmdManager.handleCommand(
                consoleSender,
                "npc spawn Rat"
            );
            future.join();  // Wait for completion
            successCount++;
            
            context.sendMessage(Message.raw(String.format("§aSpawned rat %d/5", i + 1)));
        } catch (Exception e) {
            context.sendMessage(Message.raw(String.format("§cFailed: %s", e.getMessage())));
        }
    }
    
    context.sendMessage(Message.raw(String.format("§aTotal spawned: %d/5", successCount)));
}
```

---

## Imports Required

```java
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.concurrent.CompletableFuture;
```

---

## Threading and Execution Context

### Question: Does it need to be on world thread?

**Short Answer**: Not strictly required, but recommended for NPC spawning operations.

### Explanation:

1. **Command parsing and registration**: ✓ Safe from any thread
2. **NPC/entity spawning**: ⚠️ The actual spawning happens on world thread, so best practice is to execute within world thread

### Recommended Pattern (From TriggerSpawnCommand):

```java
// Get your world reference
World world = Universe.get().getDefaultWorld();

// Defer command execution to world thread
world.execute(() -> {
    CommandManager cmdManager = HytaleServer.get().getCommandManager();
    ConsoleSender consoleSender = ConsoleSender.INSTANCE;
    
    for (int i = 0; i < 5; i++) {
        CompletableFuture<Void> future = cmdManager.handleCommand(
            consoleSender,
            "npc spawn Rat"
        );
        future.join();  // Block on world thread
    }
});
```

---

## Handling Multiple Executions

### Option 1: Sequential Execution (Blocking)
```java
for (int i = 0; i < 5; i++) {
    cmdManager.handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat")
        .join();  // Wait for each
}
```

### Option 2: Parallel Execution (Non-Blocking)
```java
List<CompletableFuture<Void>> futures = new ArrayList<>();

for (int i = 0; i < 5; i++) {
    futures.add(cmdManager.handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat"));
}

// Wait for all to complete
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

### Option 3: Sequential with Chaining (Async)
```java
cmdManager.handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat")
    .thenRun(() -> cmdManager.handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat").join())
    .thenRun(() -> System.out.println("All spawns complete!"));
```

---

## Key API Classes Found

### 1. CommandManager
- **Location**: `com.hypixel.hytale.server.core.command.system.CommandManager`
- **Key Methods**:
  - `public static CommandManager get()`
  - `public CompletableFuture<Void> handleCommand(CommandSender sender, String command)`
  - `public CompletableFuture<Void> handleCommand(PlayerRef playerRef, String command)`
  - `public CompletableFuture<Void> handleCommands(CommandSender sender, Deque<String> commands)`

### 2. HytaleServer
- **Location**: `com.hypixel.hytale.server.core.HytaleServer`
- **Key Methods**:
  - `public static HytaleServer get()`
  - `public CommandManager getCommandManager()`

### 3. ConsoleSender
- **Location**: `com.hypixel.hytale.server.core.console.ConsoleSender`
- **Key Features**:
  - `public static final ConsoleSender INSTANCE` - Singleton
  - Has all permissions by default
  - Implements `CommandSender` interface

### 4. CommandSender
- **Location**: `com.hypixel.hytale.server.core.command.system.CommandSender`
- **Key Methods**:
  - `public String getDisplayName()`
  - `public UUID getUuid()`
  - `public boolean hasPermission(String permission)`

---

## Usage Patterns from Existing Code

### From TriggerSpawnCommand (Actual Plugin Code):

The existing `TriggerSpawnCommand` shows the recommended pattern:

```java
@Override
protected void executeSync(@NonNullDecl CommandContext context) {
    // Get command arguments (safe on command thread)
    String entityType = ENTITY_TYPE.get(context);
    int count = COUNT.get(context);
    
    // Get the world
    World targetWorld = Universe.get().getDefaultWorld();
    
    if (targetWorld == null) {
        context.sendMessage(Message.raw("Error: No world available."));
        return;
    }
    
    // Defer to world thread for spawning
    targetWorld.execute(() -> {
        // NOW on world thread - safe to spawn
        for (int i = 0; i < count; i++) {
            try {
                // Spawn NPC entity directly OR use command execution
                NPCEntity entity = new NPCEntity(targetWorld);
                NPCEntity spawned = targetWorld.spawnEntity(entity, position, rotation);
                
                if (spawned != null) {
                    spawned.setRoleName(entityType);
                    totalSpawned++;
                }
            } catch (Exception e) {
                failedSpawns++;
                context.sendMessage(Message.raw("§c[DEBUG] Exception: " + e.getMessage()));
            }
        }
    });
}
```

---

## Provided Utility Class: CommandExecutor

A ready-to-use utility class has been created at:
`src/main/java/me/voidyll/utils/CommandExecutor.java`

### Usage Examples:

```java
// Simple single command
CommandExecutor.executeCommandSync("npc spawn Rat");

// Spawn multiple mobs
int spawned = CommandExecutor.spawnMultipleEntities("Rat", 5, context);

// Sequential execution with feedback
List<String> commands = Arrays.asList(
    "npc spawn Rat",
    "npc spawn Goblin",
    "npc spawn Spider"
);
int success = CommandExecutor.executeCommandsSequential(commands, context);

// Execute on specific world thread
CommandExecutor.executeCommandsOnWorldThread(world, commands, context);

// Async execution
CommandExecutor.executeCommandsParallel(commands)
    .thenRun(() -> System.out.println("All complete!"));
```

---

## Example Command: SpawnMobCommand

A complete example command has been created at:
`src/main/java/me/voidyll/commands/SpawnMobCommand.java`

This command demonstrates:
- Taking entity type and count as arguments
- Input validation
- Using CommandExecutor to spawn multiple entities
- Providing feedback messages

**Usage**: `/spawn-mob Rat 5`

---

## Summary Table

| Aspect | Details |
|--------|---------|
| **Execution Method** | `CommandManager.handleCommand(CommandSender, String)` |
| **Get Instance** | `HytaleServer.get().getCommandManager()` |
| **Command Sender** | `ConsoleSender.INSTANCE` (has all permissions) |
| **Return Type** | `CompletableFuture<Void>` |
| **Blocking** | Use `.join()` on the future |
| **Non-Blocking** | Don't call `.join()`, or use `.thenRun()` chain |
| **World Thread?** | Recommended for entity spawning (use `world.execute()`) |
| **Multiple Spawns** | Loop + `.join()` for sequential, or collect futures for parallel |
| **Example Command** | `"npc spawn Rat"` |
| **Permission** | ConsoleSender has all permissions |

---

## What Was Researched

✓ CommandManager class and methods
✓ HytaleServer API and CommandManager access
✓ ConsoleSender singleton implementation
✓ CommandSender interface hierarchy
✓ Return type (CompletableFuture)
✓ Threading model and execution context
✓ Existing command usage patterns in TriggerSpawnCommand
✓ NPC entity spawning patterns
✓ World thread execution requirements
✓ Multiple execution handling

---

## Files Created

1. **`COMMAND_EXECUTION_RESEARCH.md`** - Detailed research findings
2. **`src/main/java/me/voidyll/utils/CommandExecutor.java`** - Utility class for command execution
3. **`src/main/java/me/voidyll/commands/SpawnMobCommand.java`** - Example command implementation

All files are ready to use and integrate into your plugin.
