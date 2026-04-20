# Hytale Command Execution Research

## Summary
Based on analysis of the Hytale Server API, here's how to execute commands programmatically:

---

## 1. **Command Execution Method: `CommandManager.handleCommand()`**

### Primary Method to Execute Commands Programmatically:
```java
CommandManager.handleCommand(CommandSender sender, String command)
  → Returns: CompletableFuture<Void>
```

### Available Method Signatures:
```java
// Execute as a specific player
public CompletableFuture<Void> handleCommand(PlayerRef playerRef, String command)

// Execute as console or any command sender
public CompletableFuture<Void> handleCommand(CommandSender sender, String command)
```

---

## 2. **Getting Access to CommandManager**

### Method 1: Get from HytaleServer (Recommended for Plugins)
```java
HytaleServer.get().getCommandManager()
```

### Method 2: Direct Singleton Access
```java
CommandManager.get()
```

### Class Hierarchy:
- **HytaleServer**: `public static HytaleServer get()`
  - Has field: `private final CommandManager commandManager`
  - Method: `public CommandManager getCommandManager()`

---

## 3. **Required Imports and Setup**

```java
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.concurrent.CompletableFuture;
```

---

## 4. **Code Examples**

### Example 1: Execute Command as Console (Simple)
```java
// Get the command manager
CommandManager cmdManager = HytaleServer.get().getCommandManager();

// Use the console sender (has admin permissions)
CommandSender consoleSender = ConsoleSender.INSTANCE;

// Execute command
CompletableFuture<Void> future = cmdManager.handleCommand(
    consoleSender, 
    "npc spawn Rat"
);

// Wait for completion
future.join(); // Blocks until complete
```

### Example 2: Execute Multiple Commands (Spawning Multiple Mobs)
```java
public void spawnMultipleMobs(int count, String mobType) {
    CommandManager cmdManager = HytaleServer.get().getCommandManager();
    CommandSender consoleSender = ConsoleSender.INSTANCE;
    
    for (int i = 0; i < count; i++) {
        String command = String.format("npc spawn %s", mobType);
        
        CompletableFuture<Void> future = cmdManager.handleCommand(
            consoleSender, 
            command
        );
        
        // Optional: chain them sequentially
        future.join(); // Wait for each to complete
        
        // Or collect for parallel execution:
        // futures.add(future);
    }
}
```

### Example 3: Execute as Player
```java
// From within a command context, you have PlayerRef
PlayerRef playerRef = context.senderAsPlayerRef();

CommandManager cmdManager = HytaleServer.get().getCommandManager();

CompletableFuture<Void> future = cmdManager.handleCommand(
    playerRef,
    "npc spawn Rat"
);

future.join();
```

### Example 4: Async Execution (Non-Blocking)
```java
CommandManager cmdManager = HytaleServer.get().getCommandManager();
CommandSender consoleSender = ConsoleSender.INSTANCE;

// Execute without waiting
CompletableFuture<Void> future = cmdManager.handleCommand(
    consoleSender,
    "npc spawn Rat"
);

// Chain additional operations
future.thenRun(() -> {
    System.out.println("Command completed!");
}).thenRun(() -> {
    System.out.println("Ready for next command");
});

// Or wait later
// future.join();
```

### Example 5: Execute from World Thread (Real-World Plugin Pattern)
```java
// This is the correct pattern used in TriggerSpawnCommand
public void executeCommandOnWorldThread(World world, String command) {
    world.execute(() -> {
        // Now on the world thread
        CommandManager cmdManager = HytaleServer.get().getCommandManager();
        CommandSender consoleSender = ConsoleSender.INSTANCE;
        
        CompletableFuture<Void> future = cmdManager.handleCommand(
            consoleSender,
            command
        );
        
        // Join to ensure completion before world thread continues
        future.join();
    });
}
```

---

## 5. **Threading and Execution Context**

### Current Thread Execution:
- **Command Thread**: Commands execute on the command thread (safe)
- **World Thread**: If you need world access, defer to world thread with `world.execute()`
- **Blocking vs Non-Blocking**: 
  - `future.join()` blocks until complete
  - Without `join()`, execution is asynchronous

### Best Practices:
1. **For simple commands**: Use `handleCommand()` from any thread
2. **For multiple commands**: 
   - Collect futures and wait at end: `futures.stream().map(CompletableFuture::join).collect(toList())`
   - Or chain sequentially with `future.thenCompose()`
3. **For world access**: Execute within `world.execute(() -> { /* command here */ })`

---

## 6. **CommandManager API Methods**

```java
// Main execution method
public CompletableFuture<Void> handleCommand(CommandSender sender, String command)
public CompletableFuture<Void> handleCommand(PlayerRef playerRef, String command)

// For batch commands (multiple commands)
public CompletableFuture<Void> handleCommands(CommandSender sender, Deque<String> commands)

// Command registration
public CommandRegistration register(AbstractCommand command)
public void registerSystemCommand(AbstractCommand command)

// Access registered commands
public Map<String, AbstractCommand> getCommandRegistration()
```

---

## 7. **Complete Example: Spawn Multiple Rats with Proper Error Handling**

```java
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public void spawnRatsCommand(CommandContext context) {
    try {
        CommandManager cmdManager = HytaleServer.get().getCommandManager();
        ConsoleSender consoleSender = ConsoleSender.INSTANCE;
        
        int ratCount = 5;
        int successCount = 0;
        
        for (int i = 0; i < ratCount; i++) {
            try {
                CompletableFuture<Void> future = cmdManager.handleCommand(
                    consoleSender,
                    "npc spawn Rat"
                );
                
                // Wait for this command to complete
                future.join();
                successCount++;
                
                context.sendMessage(Message.raw(
                    String.format("§aSpawned rat %d/%d", i + 1, ratCount)
                ));
                
            } catch (Exception e) {
                context.sendMessage(Message.raw(
                    String.format("§cFailed to spawn rat: %s", e.getMessage())
                ));
            }
        }
        
        context.sendMessage(Message.raw(
            String.format("§aSuccessfully spawned %d/%d rats", successCount, ratCount)
        ));
        
    } catch (Exception e) {
        context.sendMessage(Message.raw(
            String.format("§cCommand execution failed: %s", e.getMessage())
        ));
    }
}
```

---

## 8. **Key Findings from Source Analysis**

### From CommandManager Class:
```
public static CommandManager get()
public CompletableFuture<Void> handleCommand(PlayerRef playerRef, String command)
public CompletableFuture<Void> handleCommand(CommandSender sender, String command)
public CompletableFuture<Void> handleCommands(CommandSender sender, Deque<String> commands)
```

### From HytaleServer Class:
```
public static HytaleServer get()
public CommandManager getCommandManager()
```

### From ConsoleSender Class:
```
public static final ConsoleSender INSTANCE  // Singleton for console access
public static final ConsoleSender INSTANCE  // Has all permissions
```

### From TriggerSpawnCommand Usage Pattern:
- Commands should be executed within `world.execute()` if you need world access
- Use `future.join()` to ensure completion
- Chain commands with `.thenRun()` for async operations
- Use exception handling for spawning failures

---

## 9. **Does It Need World Thread?**

- **For command parsing**: No (handled by CommandManager)
- **For NPC spawning**: Yes (if the command internally needs world access)
- **For multiple spawns**: Yes, nest within world thread:
  ```java
  world.execute(() -> {
      for (int i = 0; i < 5; i++) {
          CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat")
              .join();
      }
  });
  ```

---

## 10. **Summary Table**

| Aspect | Answer |
|--------|--------|
| **Method to execute** | `CommandManager.handleCommand(CommandSender, String)` |
| **Get CommandManager** | `HytaleServer.get().getCommandManager()` |
| **Get ConsoleSender** | `ConsoleSender.INSTANCE` |
| **Return type** | `CompletableFuture<Void>` |
| **Blocking** | Use `.join()` to block, or don't for async |
| **Multiple executions** | Loop and `.join()` each, or collect futures |
| **Threading** | Safe from any thread; CommandManager handles it |
| **Example command** | `"npc spawn Rat"` |
| **Needs world thread** | Only if the command itself needs world access (yes for NPC spawn) |

