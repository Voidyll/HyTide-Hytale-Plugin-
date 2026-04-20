package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.SpawnDirectorSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SpecialTimerPauseCommand extends CommandBase {
    private final SpawnDirectorSystem spawnDirector;

    public SpecialTimerPauseCommand(SpawnDirectorSystem spawnDirector) {
        super("special-timer-pause", "Pauses the special spawn timer");
        this.spawnDirector = spawnDirector;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        spawnDirector.pauseSpecialTimer();
        context.sendMessage(Message.raw("Special spawn timer paused."));
    }
}
