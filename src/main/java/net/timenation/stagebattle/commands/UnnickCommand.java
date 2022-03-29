package net.timenation.stagebattle.commands;

import eu.thesimplecloud.api.CloudAPI;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.game.TimeGame;
import net.timenation.timespigotapi.timeplayer.TimePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnnickCommand implements CommandExecutor {

    private final TimeGame timeGame;

    public UnnickCommand(TimeGame timeGame) {
        this.timeGame = timeGame;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        TimePlayer timePlayer = TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(player);

        if (!CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(player.getUniqueId()).getConnectedServer().isLobby()) {
            if (timePlayer.isNicked()) {
                TimeSpigotAPI.getInstance().getNickManager().unnick(player, timeGame);
            }
        }

        return false;
    }
}