package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.SpawnDirectorSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HordeTimerRestartCommand extends CommandBase {
    private final SpawnDirectorSystem spawnDirector;

    public HordeTimerRestartCommand(SpawnDirectorSystem spawnDirector) {
        super("horde-timer-restart", "Restarts the horde spawn timer with a new random value");
        this.spawnDirector = spawnDirector;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        spawnDirector.restartHordeTimer();
        long remainingSeconds = spawnDirector.getHordeTimerRemainingMs() / 1000;
        context.sendMessage(Message.raw("Horde spawn timer restarted. Next horde in " + remainingSeconds + " seconds."));
    }
}
