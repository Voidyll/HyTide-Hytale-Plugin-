package me.voidyll.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import me.voidyll.systems.SpawnDirectorSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HordeTimerUnpauseCommand extends CommandBase {
    private final SpawnDirectorSystem spawnDirector;

    public HordeTimerUnpauseCommand(SpawnDirectorSystem spawnDirector) {
        super("horde-timer-unpause", "Resumes the horde spawn timer");
        this.spawnDirector = spawnDirector;
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        spawnDirector.unpauseHordeTimer();
        context.sendMessage(Message.raw("Horde spawn timer resumed."));
    }
}
