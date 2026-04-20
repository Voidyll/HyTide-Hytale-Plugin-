package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.SpawnDirectorSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SpecialTimerRestartCommand extends CommandBase {
    private final SpawnDirectorSystem spawnDirector;

    public SpecialTimerRestartCommand(SpawnDirectorSystem spawnDirector) {
        super("special-timer-restart", "Restarts the special spawn timer");
        this.spawnDirector = spawnDirector;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        spawnDirector.restartSpecialTimer();
        long remainingSeconds = spawnDirector.getSpecialTimerRemainingMs() / 1000;
        context.sendMessage(Message.raw("Special spawn timer restarted. Next special in " + remainingSeconds + " seconds."));
    }
}
