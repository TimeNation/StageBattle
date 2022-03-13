package net.timenation.stagebattle.listener;

import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.language.I18n;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().updateTimeStatsPlayer(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle"));

        if(StageBattle.getInstance().getGameState().equals(GameState.LOBBY) || StageBattle.getInstance().getGameState().equals(GameState.STARTING)) {
            Bukkit.getOnlinePlayers().forEach(current -> {
                current.sendMessage(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.messages.quit", TimeSpigotAPI.getInstance().getRankManager().getPlayersRankAndName(player.getUniqueId()), Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers()));
            });

            if(Bukkit.getOnlinePlayers().size() - 1 < StageBattle.getInstance().getNeededPlayers()) {
                StageBattle.getInstance().getCountdownManager().stopCountdown();
            }
        } else if(StageBattle.getInstance().getGameState() == GameState.ENDING) {
            if(Bukkit.getOnlinePlayers().size() <= 1) {
                Bukkit.shutdown();
            }
        } else {
            StageBattle.getInstance().getPlayers().remove(player);
            TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").setDeaths(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").getDeaths() + 1);
            TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").setLooses(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").getLooses() + 1);
            if(Bukkit.getOnlinePlayers().size() - 1 == 1 || StageBattle.getInstance().getPlayers().size() == 1) {
                Bukkit.getOnlinePlayers().forEach(current -> {
                    current.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                    current.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
                    current.getInventory().clear();
                    current.setGameMode(GameMode.SURVIVAL);
                    current.sendTitle(I18n.format(current, "api.game.title.loose.top"), I18n.format(current, "api.game.title.loose.bottom"));
                    StageBattle.getInstance().getDefaultGameQuitItem().setItem(current);
                    TimeSpigotAPI.getInstance().getTimePlayerManager().updateTimePlayer(TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(current));
                });

                for(Player winner : StageBattle.getInstance().getPlayers()) {
                    winner.sendMessage(I18n.format(player, "api.game.actionbar.playerhaswongame", StageBattle.getInstance().getPrefix()));
                    winner.sendTitle(I18n.format(player, "api.game.title.win.top"), I18n.format(player, "api.game.title.win.bottom"));
                    winner.sendMessage(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.messages.playerhaswongame", 300));
                    TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(winner).setCoins(TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(winner).getCoins() + 300);
                    TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").setWins(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle").getWins() + 1);
                }

                StageBattle.getInstance().setGameState(GameState.ENDING);
                StageBattle.getInstance().getCountdownManager().startEndountdown();
            }
        }
    }
}
