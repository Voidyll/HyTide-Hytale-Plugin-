package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.SpawnDirectorSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SpecialTimerUnpauseCommand extends CommandBase {
    private final SpawnDirectorSystem spawnDirector;

    public SpecialTimerUnpauseCommand(SpawnDirectorSystem spawnDirector) {
        super("special-timer-unpause", "Resumes the special spawn timer");
        this.spawnDirector = spawnDirector;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        spawnDirector.unpauseSpecialTimer();
        context.sendMessage(Message.raw("Special spawn timer resumed."));
    }
}
