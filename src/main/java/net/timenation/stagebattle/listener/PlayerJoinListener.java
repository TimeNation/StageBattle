package net.timenation.stagebattle.listener;

import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.ItemManager;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.language.I18n;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle");

        if(StageBattle.getInstance().getGameState().equals(GameState.LOBBY) || StageBattle.getInstance().getGameState().equals(GameState.STARTING)) {
            player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));

            if(Bukkit.getOnlinePlayers().size() == StageBattle.getInstance().getNeededPlayers()) {
                StageBattle.getInstance().getCountdownManager().startCountdown();
            }

            Bukkit.getOnlinePlayers().forEach(current -> {
                current.sendMessage(I18n.format(current, StageBattle.getInstance().getPrefix(), "api.game.messages.join", TimeSpigotAPI.getInstance().getRankManager().getPlayersRankAndName(player.getUniqueId()), Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers()));
                StageBattle.getInstance().getScoreboardManager().sendLobbyScoreboardToPlayer(current, StageBattle.getInstance().getCountdownManager().getCountdown());
            });

            return;
        }

        StageBattle.getInstance().getSpecatePlayers().add(player);
        Bukkit.getOnlinePlayers().forEach(current -> {
            current.hidePlayer(StageBattle.getInstance(), player);
        });
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemManager(Material.COMPASS, 1).setDisplayName(I18n.format(player, "api.game.item.teleporter")).build());
        StageBattle.getInstance().getDefaultGameQuitItem().setItem(player);
        TimeSpigotAPI.getInstance().getTablistManager().registerRankTeam(player, "§c✗ §8» ", "", ChatColor.GRAY, 18);
        player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
        player.setAllowFlight(true);
    }
}
