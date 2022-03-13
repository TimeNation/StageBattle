package net.timenation.stagebattle.commands;

import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.language.I18n;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        if (player.hasPermission("timenation.forcestart")) {
            if (Bukkit.getOnlinePlayers().size() >= StageBattle.getInstance().getNeededPlayers()) {
                if (StageBattle.getInstance().getCountdownManager().getCountdown() > 10) {
                    StageBattle.getInstance().getCountdownManager().startGame();
                    player.sendMessage(I18n.format(player, "api.game.messages.startgame", StageBattle.getInstance().getPrefix()));
                } else {
                    if (StageBattle.getInstance().getGameState().equals(GameState.INGAME)) {
                        player.sendMessage(I18n.format(player, "api.game.messages.isingame", StageBattle.getInstance().getPrefix()));
                    } else if (StageBattle.getInstance().getGameState().equals(GameState.ENDING)) {
                        player.sendMessage(I18n.format(player, "api.game.messages.gameisended", StageBattle.getInstance().getPrefix()));
                    } else {
                        player.sendMessage(I18n.format(player, "api.game.messages.gamealreadystarts", StageBattle.getInstance().getPrefix()));
                    }
                }
            } else {
                player.sendMessage(I18n.format(player, "api.game.messages.notenoughplayerstostart", StageBattle.getInstance().getPrefix()));
            }
        } else {
            player.sendMessage(I18n.format(player, "api.velocity.messages.nopermissions", StageBattle.getInstance().getPrefix()));
        }
        return false;
    }
}
