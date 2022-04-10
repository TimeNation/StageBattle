package net.timenation.stagebattle.listener;

import net.timenation.stagebattle.StageBattle;
import net.timenation.timespigotapi.TimeSpigotAPI;
import net.timenation.timespigotapi.manager.ItemManager;
import net.timenation.timespigotapi.manager.game.gamestates.GameState;
import net.timenation.timespigotapi.manager.language.I18n;
import net.timenation.timespigotapi.timeplayer.TimePlayer;
import net.timenation.timespigotapi.timeplayer.TimeStatsPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        TimeStatsPlayer timeStatsPlayer = TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(player, "StageBattle");

        event.setDeathMessage(null);

        switch (player.getMetadata("lives").get(0).asInt()) {
            case 5:
                player.setMetadata("lives", new FixedMetadataValue(StageBattle.getInstance(), Integer.valueOf(4)));
                break;
            case 4:
                player.setMetadata("lives", new FixedMetadataValue(StageBattle.getInstance(), Integer.valueOf(3)));
                break;
            case 3:
                player.setMetadata("lives", new FixedMetadataValue(StageBattle.getInstance(), Integer.valueOf(2)));
                break;
            case 2:
                player.setMetadata("lives", new FixedMetadataValue(StageBattle.getInstance(), Integer.valueOf(1)));
                break;
            case 1:
                player.setMetadata("lives", new FixedMetadataValue(StageBattle.getInstance(), Integer.valueOf(0)));
                break;
        }

        timeStatsPlayer.setDeaths(timeStatsPlayer.getDeaths() + 1);

        if(!(player.getKiller() instanceof Player)) {
            Bukkit.getOnlinePlayers().forEach(current -> {
                current.sendMessage(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.messages.player.death.4", TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(player.getUniqueId()).getPlayersNameWithRankColor(player.getUniqueId())));
                StageBattle.getInstance().getIngameScoreboard().sendIngameScoreboard(player, StageBattle.getInstance());
            });

            if(player.getMetadata("lives").get(0).asInt() > 0) {
                player.spigot().respawn();
                Bukkit.getScheduler().runTaskLater(StageBattle.getInstance(), () -> {
                    player.teleport(new Location(StageBattle.getInstance().getWorld(), 166.5, 21, -143.5, 90, 0));
                    player.getInventory().addItem(new ItemManager(Material.SCAFFOLDING, 16).build());
                }, 10L);
            } else {
                StageBattle.getInstance().getPlayers().remove(player);
                StageBattle.getInstance().getSpecatePlayers().add(player);

                if (StageBattle.getInstance().getPlayers().size() == 1) {
                    StageBattle.getInstance().getCountdownManager().startEndountdown();
                    for (Player winner : StageBattle.getInstance().getPlayers()) {
                        TimePlayer timeWinner = TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(winner);
                        TimeStatsPlayer timeStatsWinner = TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(winner, "StageBattle");
                        Bukkit.getOnlinePlayers().forEach(current -> {
                            player.spigot().respawn();
                            player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                            if (current != winner) {
                                current.playSound(current.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
                            }
                            current.getInventory().clear();
                            StageBattle.getInstance().getDefaultGameQuitItem().setItem(current);
                            current.showPlayer(current);
                            current.sendTitle(I18n.format(current, "api.game.title.loose.top"), I18n.format(current, "api.game.title.loose.bottom", (Object) TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(winner.getUniqueId()).getPlayersNameWithRankColor(winner.getUniqueId())));
                            StageBattle.getInstance().getScoreboardManager().sendEndScoreboardToPlayer(current, winner);
                            if(current != winner) TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").setLooses(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").getLooses() + 1);
                            TimeSpigotAPI.getInstance().getTimePlayerManager().updateTimePlayer(TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(current));
                        });
                        timeWinner.setCoins(timeWinner.getCoins() + 300);
                        timeStatsWinner.setWins(timeStatsWinner.getWins() + 1);
                        winner.sendTitle(I18n.format(winner, "api.game.title.win.top"), I18n.format(winner, "api.game.title.win.bottom"));
                        winner.sendMessage(I18n.format(winner, StageBattle.getInstance().getPrefix(), "api.game.messages.playerhaswongame", 300));
                    }
                } else {
                    Bukkit.getScheduler().runTaskLater(StageBattle.getInstance(), () -> {
                        player.spigot().respawn();
                        player.setGameMode(GameMode.CREATIVE);
                        player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                        player.getInventory().setItem(0, new ItemManager(Material.COMPASS, 1).setDisplayName(I18n.format(player, "api.game.item.teleporter")).build());
                        timeStatsPlayer.setLooses(timeStatsPlayer.getLooses() + 1);
                        StageBattle.getInstance().getDefaultGameQuitItem().setItem(player);
                    }, 10);
                }
            }

            return;
        }

        Player killer = player.getKiller();
        TimePlayer timeKiller = TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(killer);
        TimeStatsPlayer timeStatsKiller = TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(killer, "StageBattle");

        killer.getInventory().addItem(new ItemManager(Material.SCAFFOLDING, 16).build());

        Bukkit.getOnlinePlayers().forEach(current -> {
            current.sendMessage(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.messages.player.killedbyplayer", TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(player.getUniqueId()).getPlayersNameWithRankColor(player.getUniqueId()), TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(killer.getUniqueId()).getPlayersNameWithRankColor(killer.getUniqueId())));
            StageBattle.getInstance().getIngameScoreboard().sendIngameScoreboard(player, StageBattle.getInstance());
        });
        
        killer.sendMessage(I18n.format(player, StageBattle.getInstance().getPrefix(), "api.game.actionbar.playerkilledplayer", TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(player.getUniqueId()).getPlayersNameWithRankColor(player.getUniqueId())));
        timeKiller.setCoins(timeKiller.getCoins() + 100);
        timeStatsKiller.setKills(timeStatsKiller.getKills() + 1);
        
        if(player.getMetadata("lives").get(0).asInt() > 0) {
            player.spigot().respawn();
            Bukkit.getScheduler().runTaskLater(StageBattle.getInstance(), () -> {
                player.teleport(new Location(StageBattle.getInstance().getWorld(), 166.5, 21, -143.5, 90, 0));
                player.getInventory().addItem(new ItemManager(Material.SCAFFOLDING, 16).build());
            }, 10L);
        } else {
            StageBattle.getInstance().getPlayers().remove(player);
            StageBattle.getInstance().getSpecatePlayers().add(player);

            if (StageBattle.getInstance().getPlayers().size() == 1) {
                StageBattle.getInstance().getCountdownManager().startEndountdown();
                timeKiller.setCoins(timeKiller.getCoins() + 300);
                timeStatsKiller.setWins(timeStatsKiller.getWins() + 1);
                StageBattle.getInstance().setGameState(GameState.ENDING);
                Bukkit.getOnlinePlayers().forEach(current -> {
                    player.spigot().respawn();
                    player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                    current.getInventory().clear();
                    StageBattle.getInstance().getDefaultGameQuitItem().setItem(current);
                    current.sendTitle(I18n.format(current, "api.game.title.loose.top"), I18n.format(current, "api.game.title.loose.bottom", (Object) TimeSpigotAPI.getInstance().getRankManager().getPlayersRank(killer.getUniqueId()).getPlayersNameWithRankColor(killer.getUniqueId())));
                    current.showPlayer(current);
                    StageBattle.getInstance().getScoreboardManager().sendEndScoreboardToPlayer(current, killer);
                    if(current != killer) TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").setLooses(TimeSpigotAPI.getInstance().getTimeStatsPlayerManager().getTimeStatsPlayer(current, "StageBattle").getLooses() + 1);
                    TimeSpigotAPI.getInstance().getTimePlayerManager().updateTimePlayer(TimeSpigotAPI.getInstance().getTimePlayerManager().getTimePlayer(current));
                });
                killer.sendTitle(I18n.format(killer, "api.game.title.win.top"), I18n.format(killer, "api.game.title.win.bottom"));
                killer.sendMessage(I18n.format(killer, StageBattle.getInstance().getPrefix(), "api.game.messages.playerhaswongame", 300));
                killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 0);
            } else {
                Bukkit.getScheduler().runTaskLater(StageBattle.getInstance(), () -> {
                    player.spigot().respawn();
                    player.setGameMode(GameMode.CREATIVE);
                    player.teleport(new Location(Bukkit.getWorld("world"), 111.5, 114.00, -262.5, -45, 0));
                    player.getInventory().setItem(0, new ItemManager(Material.COMPASS, 1).setDisplayName(I18n.format(player, "api.game.item.teleporter")).build());
                    timeStatsPlayer.setLooses(timeStatsPlayer.getLooses() + 1);
                    StageBattle.getInstance().getDefaultGameQuitItem().setItem(player);
                }, 10);
            }
        }
    }
}
