package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.SpawnDirectorSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HordeTimerPauseCommand extends CommandBase {
    private final SpawnDirectorSystem spawnDirector;

    public HordeTimerPauseCommand(SpawnDirectorSystem spawnDirector) {
        super("horde-timer-pause", "Pauses the horde spawn timer");
        this.spawnDirector = spawnDirector;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        spawnDirector.pauseHordeTimer();
        context.sendMessage(Message.raw("Horde spawn timer paused."));
    }
}
